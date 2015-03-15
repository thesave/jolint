package trunk

import jolie.CommandLineParser
import jolie.lang.parse.SemanticVerifier.Configuration
import jolie.lang.parse._
import jolie.lang.parse.ast._
import jolie.lang.parse.util.{ParsingUtils, ProgramInspector}

import scala.collection.JavaConversions

/*************************************************************************
*	Copyright (C) 2015 Saverio Giallorenzo saverio.giallorenzo@gmail.com  *
*                                                                       *
* This program is free software: you can redistribute it and/or modify  *
* it under the terms of the GNU General Public License as published by  *
* the Free Software Foundation, either version 3 of the License, or     *
* (at your option) any later version.                                   *
*                                                                       *
* This program is distributed in the hope that it will be useful,       *
* but WITHOUT ANY WARRANTY; without even the implied warranty of        *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          *
* GNU General Public License for more details.                          *
*************************************************************************/

object jolint{

  var outputPortList = Map[ String, OutputPortInfo ]()
  var inputOperationList = List[ OperationDeclaration ]()

  def hasOperation[ T <: OperationDeclaration ](
                                                 operation: String,
                                                 opDeclarations: List[ OperationDeclaration ],
                                                 operationClass: Class[ T ] ): Boolean = {
    opDeclarations.find( op => op.id().equals( operation ) ) match {
      case Some( op ) => op.getClass.equals( operationClass )
      case None => false
    }
  }

  def toScalaList( list: java.util.List[ OLSyntaxNode ] ) =
    JavaConversions.asScalaBuffer( list )

  def unfoldProgram( oLSyntaxNode: OLSyntaxNode,
                     i: ProgramInspector ): Unit = {
    oLSyntaxNode match {
      case node: Program =>
        toScalaList( node.children() )
          .filter( _.isInstanceOf[ DefinitionNode ] )
          .foreach( child => unfoldProgram( child,i ) )

      case node: DefinitionNode => unfoldProgram( node.body(), i )

      case node: ParallelStatement => {
        toScalaList( node.children() ).
          foreach( subnode => unfoldProgram( subnode, i ) )
      }

      case node: SequenceStatement => {
        toScalaList( node.children() ).
          foreach( subnode => unfoldProgram( subnode, i ) )
      }

      case node: SolicitResponseOperationStatement => {
        if( outputPortList.contains( node.outputPortId() ) ){
          if( !hasOperation( node.id(),
            JavaConversions.collectionAsScalaIterable( outputPortList( node.outputPortId() ).operations() ).toList,
            classOf[ RequestResponseOperationDeclaration ] ) ) {
            println(
              node.context().source + ":" + node.context().line() +
                ": error: RequestResponse operation \"" + node.id() + "\" not declared in port " + node.outputPortId())
          }
        } else {
          println( node.context().source + ":" + node.context().line() +
            ": error: OutputPort \"" + node.outputPortId() + "\" not declared"
          )
        }
      }

      case node: NotificationOperationStatement => {
        if ( outputPortList.contains( node.outputPortId() ) ) {
          if ( !hasOperation(
            node.id(),
            JavaConversions.collectionAsScalaIterable( outputPortList(node.outputPortId() ).operations() ).toList,
            classOf[ OneWayOperationDeclaration ] ) ) {
            println(
              node.context().source + ":" + node.context().line() +
                ": error: OneWay operation \"" + node.id() + "\" not declared in outputPort " + node.outputPortId())
          }
        } else {
          println(node.context().source + ":" + node.context().line() +
            ": error: OutputPort \"" + node.outputPortId() + "\" not declared"
          )
        }
      }

      case node: OneWayOperationStatement => {
        if( !hasOperation(
          node.id(),
          inputOperationList,
          classOf[ OneWayOperationDeclaration ] ) ){
          println(
            node.context().source + ":" + node.context().line() +
              ": error: OneWay operation " + node.id() + " not declared in any inputPort" )
        }
      }

      case node: RequestResponseOperationStatement => {
        if( !hasOperation(
          node.id(),
          inputOperationList,
          classOf[ RequestResponseOperationDeclaration ] ) ){
          println(
            node.context().source + ":" + node.context().line() +
              ": error: RequestResponse operation " + node.id() + " not declared in any inputPort" )
        }
      }

      case default =>
    }
  }

  def main( args: Array[ String ] ): Unit = {
    // read commandline
    val cp = new CommandLineParser( args, this.getClass.getClassLoader )

    // init olParser
    val olParser = new OLParser(
      new Scanner( cp.programStream(),
        java.net.URI.create( "file:" + cp.programFilepath() ),
        null
      ),
      cp.includePaths(),
      cp.jolieClassLoader()
    )

    // parse and optimize tree of program
    val program = new OLParseTreeOptimizer( olParser.parse() ).optimize()
    val inspector = ParsingUtils.createInspector( program )

    inspector.getOutputPorts().foreach( outputPort =>
      outputPortList = outputPortList + ( ( outputPort.id(), outputPort ) )
    )

    inspector.getInputPorts().foreach( inputPort =>
      inputOperationList = JavaConversions.collectionAsScalaIterable( inputPort.operations() ).toList
    )

    unfoldProgram( program, inspector )

    val configuration = new Configuration()
    configuration.setCheckForMain( false )
    new SemanticVerifier( program, configuration ).validate()

  }
}