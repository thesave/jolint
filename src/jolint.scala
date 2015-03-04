import java.io.InputStream
import jolie.lang.parse.Scanner.Token
import jolie.lang.parse._
import jolie.lang.parse.ast._
import jolie.CommandLineParser
import jolie.lang.parse.util.{ParsingUtils, ProgramInspector}

import scala.collection.JavaConversions
import scala.collection.JavaConverters._

/************************************************************************
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

class Jolint {

}


object JolintObject extends Jolint{

  var outputPortList = scala.collection.mutable.Map[ String, OutputPortInfo ]()
  var inputOperationList = List[ OperationDeclaration ]()

  def parseProgram(
        inputStream: InputStream, source: java.net.URI, includePath: Array[ String ],
        classLoader: ClassLoader, definedCostants: Map[ String, Token ]
  ): Program = {
    val olParser = new OLParser(new Scanner(inputStream, source), includePath, classLoader)
    new OLParseTreeOptimizer(olParser.parse()).optimize()
  }

  def hasInputOperation[ T <: OperationDeclaration ]( operation: String,
                   opDeclarations: List[ OperationDeclaration ],
                   operationClass: Class[ T ] ): Boolean = {
    opDeclarations.foreach(
      opDecl =>
      {
        if( opDecl.id().equals( operation ) && opDecl.getClass.equals( operationClass ) ){
          return true
        }
      }
    )
    false
  }


  def hasOutputOperation[ T <: OperationDeclaration ]( operation: String,
                    opDeclarations: java.util.Collection[ OperationDeclaration ],
                    operationClass: Class[ T ] ): Boolean = {
    JavaConversions.collectionAsScalaIterable( opDeclarations ).foreach(
      opDecl =>
      {
        if( opDecl.id().equals( operation ) && opDecl.getClass.equals( operationClass ) ){
         return true
        }
      }
    )
    false
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
          if( !hasOutputOperation( node.id(),
            outputPortList( node.outputPortId() ).operations(),
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
        if (outputPortList.contains(node.outputPortId())) {
          if (!hasOutputOperation(node.id(),
            outputPortList(node.outputPortId()).operations(), classOf[ OneWayOperationDeclaration ] ) ) {
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
        if( !hasInputOperation( node.id(), inputOperationList, classOf[ OneWayOperationDeclaration ] ) ){
          println(
            node.context().source + ":" + node.context().line() +
              ": error: OneWay operation " + node.id() + " not declared in any inputPort" )
        }
      }

      case node: RequestResponseOperationStatement => {
        if( !hasInputOperation( node.id(), inputOperationList, classOf[ RequestResponseOperationDeclaration ] ) ){
          println(
            node.context().source + ":" + node.context().line() +
              ": error: RequestResponse operation " + node.id() + " not declared in any inputPort" )
        }
      }

      case default => return
    }
  }

  def main( args: Array[ String ] ): Unit = {
    // read commandline
    val cp = new CommandLineParser( args, this.getClass.getClassLoader )

    // init olParser
    val olParser = new OLParser(
      new Scanner( cp.programStream(),
      java.net.URI.create( "file:" + cp.programFilepath() ) ),
      cp.includePaths(),
    cp.jolieClassLoader()
    )

    // parse and optimize tree of program
    val program = new OLParseTreeOptimizer( olParser.parse() ).optimize()
    val inspector = ParsingUtils.createInspector( program )

    inspector.getOutputPorts().foreach( outputPort =>
      outputPortList += ( outputPort.id -> outputPort )
    )

    inspector.getInputPorts().foreach( inputPort =>
      JavaConversions.collectionAsScalaIterable( inputPort.operations() ).foreach( operation =>
      inputOperationList = inputOperationList.::( operation )
      )
    )

    unfoldProgram( program, inspector )

    //val sv = new SemanticVerifier( program )

  }
}