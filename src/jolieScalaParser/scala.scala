package jolieScalaParser

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


import scala.util.parsing.combinator.JavaTokenParsers

import _root_.scala.io.Source
import _root_.scala.util.parsing.combinator.JavaTokenParsers

sealed trait Program
case class Main( pars: List[ Parallel ] ) extends Program
case class Parallel( seqs: List[Sequence] ) extends Program
case class Sequence( stats: List[ String ]) extends Program

class JolieParser extends JavaTokenParsers {

  val MAIN = "main"
  val INIT = "init"
  val LC = "{"
  val RC = "}"
  val LR = "("
  val RR = ")"
  val LS = "["
  val RS = "]"
  val COMMA = ","
  val COLON = ":"
  val SEQ = ";"
  val PAR = "|"
  val AT = "@"
  val ASSIGN = "="
  val DOT = "."
  val NOT = "!"
  val QUESTION = "?"
  val PLUS = "+"
  val MINUS = "-"
  val TIMES = "*"
  val DIVIDE = "/"
  val EQUAL = "=="
  val GT = ">"
  val LT = "<"
  val LTE = "<="
  val GTE = ">="
  val PPLUS = "++"
  val MMINUS = "--"
  val AND = "&&"
  val OR = "||"
  val SHARP = "#"
  val CARET = "^"
  val TRUE = "true"
  val FALSE = "false"
  val DOUBLERIGHTARROW = "=>"

  val INT_TYPE = "int"
  val LONG_TYPE = "long"
  val BOOL_TYPE = "bool"
  val DOUBLE_TYPE = "double"
  val STRING_TYPE = "string"
  val RAW_TYPE = "raw"
  val VOID_TYPE = "void"
  val ANY_TYPE = "any"

  val NULLPROCESS = "nullProcess"
  val EXIT = "exit"

  def program: Parser[ Any ] =
    ( declarations
      | initProcedure
      | mainProcedure
      ) ~ opt( program )

  def declarations: Parser[ Any ] =
    deployment | execution

  def deployment: Parser[ Any ] = (
    include
    | constants
    | typeDeclaration
    | interface
    | portDeclaration
    | embedded
    | cset
    | define
    )

  def include: Parser[ Any ] = "include" ~ stringLiteral

  def constants: Parser[ Any ] =
    "contants" ~ LC ~ repsep( constantAssignment, COMMA ) ~ RC

  def constantAssignment: Parser[ Any ] =
    ident ~ ASSIGN ~ ( wholeNumber | floatingPointNumber | stringLiteral | ident )

  def typeDeclaration: Parser[ Any ] =
    "type" ~ ident ~ COLON ~ typeDefinition

  def typeDefinition: Parser[ Any ] =
    ( ident | nativeType ) ~
      opt( LC ~ rep( subtype ) ~ RC )

  def subtype: Parser[ Any ] =
    DOT ~ ident ~ opt( cardinality ) ~ COLON ~ typeDefinition

  def cardinality: Parser[ Any ] = (
    QUESTION
      | TIMES
      | ( LS ~ wholeNumber ~ COMMA ~ ( wholeNumber | TIMES ) ~ RS )
    )


  def interface: Parser[ Any ] =
    "interface" ~ ident ~ LC ~ operationsDeclaration ~ RC

  def operationsDeclaration: Parser[ Any ] =(
    ( opt( onewayDeclaration ) ~ opt( requestResponseDeclaration ) )
      | ( opt( requestResponseDeclaration ) ~ opt( onewayDeclaration ) )
    )

  def onewayDeclaration: Parser[ Any ] =
    "OneWay" ~ COLON ~ repsep( oneWayOperationSignature, COMMA )

  def oneWayOperationSignature: Parser[ Any] =
    ident ~ opt( LR ~ messageType ~RR )

  def requestResponseDeclaration: Parser[ Any ] =
    "RequestResponse" ~ COLON ~ repsep( requestResponseOperationSignature, COMMA )

  def requestResponseOperationSignature: Parser[ Any ] =
    ident ~ opt( LR ~ messageType ~ RR ~ LR ~ messageType ~ RR ) ~
      opt( "throws" ~ rep( fault ) )

  def fault: Parser[ Any ] =
    ident ~ opt( LR ~ messageType ~ RR )

  def messageType: Parser[ Any ] =
    ident | nativeType


  def portDeclaration: Parser[ Any ] =
    inputPortDefinition | outputPortDefinition

  def inputPortDefinition: Parser[ Any ] =
    "inputPort" ~ ident ~ LC
  inputPortContent ~ RC

  def inputPortContent: Parser[ Any ] =
    ( location
      | protocol
      | oneWayOperationSignature
      | requestResponseOperationSignature
      | interfaces
      | redirects
      | aggregates
      ) ~ opt( inputPortContent )

  def location: Parser[ Any ] =
    "Location" ~ COLON ~ ( stringLiteral | path )

  def protocol: Parser[ Any ] =
    "Protocol" ~ COLON ~ ( stringLiteral | path ) ~
      opt( LC ~ parStatement ~ RC )

  def interfaces: Parser[ Any ] =
    "Interface" ~ COLON ~ repsep( interface | ident , COMMA )

  def redirects: Parser[ Any ] =
    ident ~ DOUBLERIGHTARROW ~ ident

  def aggregates: Parser[ Any ] =
    "Aggregates" ~ COLON ~ repsep( ident, COMMA )

  def outputPortDefinition: Parser[ Any ] =
    "inputPort" ~ ident ~ LC
  outputPortContent ~ RC

  def outputPortContent: Parser[ Any ] =
    ( location
      | protocol
      | oneWayOperationSignature
      | requestResponseOperationSignature
      | interfaces
      ) ~ opt( outputPortContent )

  def embedded: Parser[ Any ] =
    ( "embedded" ~ LC ~ ( "Java" | "Jolie" | "JavaScript" ) ~ COLON ~
      repsep( stringLiteral ~ opt( "in" ~ outputPortDefinition ), COMMA )
      ~ RC )

  def cset: Parser[ Any ] =
    "cset" ~ LC ~ repsep( correlationSet, COMMA ) ~ RC

  def correlationSet: Parser[ Any ] =
    ident ~ COLON ~ rep( path )

  def define: Parser[ Any ] =
    "define" ~ ident ~ LC ~ parStatement ~ RC

  def execution: Parser[ Any ] =
    "execution" ~ LC ~ ( "single" | "concurrent" ~ "sequential" ) ~ RC

  def initProcedure: Parser[ Any ] =
    INIT ~> LC ~> parStatement <~ RC //^^ { new Main( _ ) }

  def mainProcedure: Parser[ Main ] =
    MAIN ~> LC ~> parStatement <~ RC ^^ { new Main( _ ) }

  def parStatement: Parser[ List[ Parallel ] ] =
    seqStatement ~ opt( PAR ~ parStatement ) ^^ {
      case s1 ~ s2 =>
        s2 match {
          case Some( s )  => new Parallel( List( s1 ) )::s._2
          case None => List(new Parallel(List(s1)))
        }
    }

  def seqStatement: Parser[ Sequence ] =
    basicStatement ~ opt( SEQ ~ seqStatement ) ^^ {
      case s1 ~ s2 =>
        s2 match {
          case Some( s )  => new Sequence( s1::s._2.stats )
          case None => new Sequence( List(s1) )
        }
    }

  def basicStatement: Parser[ String ] =
    ( assignment
      | NULLPROCESS
      | EXIT
      | inputOperation
      | outputOperation
      | inputChoice
      | procedureCall
      | spawn
      | jolieWith
      | undef
      | jolieIf
      | jolieWhile
      | jolieFor
      | jolieForeach
      | synchronized
      | linkIn
      | linkOut
      | scope
      | jolieThrow
      | install
      | currentHandler
      | compensate
      )^^ {
      case a => a.toString
    }

  // deployment elements


  // behaviour elements
  def assignment: Parser[ Any ] =
  { ( path ~ ASSIGN ~ expression ) | increment | decrement }


  def inputOperation: Parser[ Any ] = { requestResponse | oneway }

  def oneway: Parser[ Any ] = { ident ~ LR ~ opt( expression ) ~ RR }

  def requestResponse: Parser[ Any ] = {
    ident ~ LR ~ opt( expression ) ~ RR ~ LR ~ opt( expression ) ~ RR ~ LC ~ parStatement ~ RC }


  def outputOperation: Parser[ Any ] = { solicitResponse | notification }

  def notification: Parser[ Any ] = {
    ident ~ AT ~ opt( expression ) ~ LR ~ opt( expression ) ~ RR
  }

  def solicitResponse: Parser[ Any ] = {
    ident ~ AT ~ expression ~ LR ~ opt( expression ) ~ RR ~ LR ~ opt( expression ) ~ RR
  }


  def inputChoice: Parser[ Any ] = {
    rep ( LS ~ ( inputOperation | linkIn ) ~ RS ~ LC ~ parStatement ~RC )
  }

  def procedureCall: Parser[ Any ] = {
    ident
  }

  def spawn: Parser[ Any ] = {
    "spawn" ~ LR ~ path ~ RR ~
      "over" ~ LR ~ expression ~ RR ~
      opt( "in" ~ path ) ~
      LC ~ parStatement ~ RC
  }

  def jolieWith: Parser[ Any ] = {
    "with" ~ LR ~ path ~ RR ~
      LC ~ parStatement ~ RC
  }

  def undef: Parser[ Any ] = {
    "undef" ~ LR ~ path ~ RR
  }

  def jolieIf: Parser[ Any ] = {
    "if" ~ LR ~ expression ~ RR ~
      LC ~ parStatement ~ RC ~
      opt( rep( "else" ~ jolieIf ) ) ~
      "else" ~ LC ~ parStatement ~ RC
  }

  def jolieWhile: Parser[ Any ] = {
    "while" ~ LR ~ expression ~ RR ~
      LC ~ parStatement ~ RC
  }

  def jolieFor: Parser[ Any ] = {
    "for" ~ LR ~ parStatement ~ COMMA ~
      expression ~ COMMA ~
      parStatement ~ RR ~
      LC ~ parStatement ~ RC
  }

  def jolieForeach: Parser[ Any ] = {
    "foreach" ~ LR ~ path ~ COLON ~ path ~ RR ~ LC ~ parStatement ~ RC
  }

  def synchronized: Parser[ Any ] = {
    "synchronized" ~ LR ~ path ~ RR ~ LC ~ parStatement ~ RC
  }

  def linkIn: Parser[ Any ] = {
    "linkIn" ~ LR ~ path ~ RR
  }

  def linkOut: Parser[ Any ] = {
    "linkOut" ~ LR ~ path ~ RR
  }

  def scope: Parser[ Any ] = {
    opt( "scope" ~ LR ~ ident ~ RR ) ~
      LC ~ parStatement ~ RC
  }

  def jolieThrow: Parser[ Any ] = {
    "throw" ~ LR ~ ident ~ opt( COMMA ~ expression ) ~ RR
  }

  def install: Parser[ Any ] = {
    "install" ~ LR ~
      ( ident | "this") ~ DOUBLERIGHTARROW ~ parStatement ~
      rep( COMMA ~ install ) ~ RR
  }

  def currentHandler: Parser[ Any ] = {
    "cH"
  }

  def compensate: Parser[ Any ] = {
    "comp" ~ LR ~ ident ~ RR
  }

  def path: Parser[ Any ] =
    node ~ rep( DOT ~ path )

  def root: Parser[ Any ] = {
    ident ~ opt( rep( arrayIndex ) ) ~ opt( subPath )
  }

  def subPath: Parser[ Any ] = {
    DOT ~ ( root | lookUp )
  }

  def arrayIndex: Parser[ Any ] = {
    LS ~ expression ~RS
  }

  def lookUp: Parser[ Any ] = {
    LR ~ expression ~ RR ~
      opt( rep ( arrayIndex ) ) ~
      opt( subPath )
  }

  def node: Parser[ Any ] =
    ident

  def expression: Parser[ Any ] =
    terExpression ~ opt( nextExpression )
  def nextExpression: Parser[ Any ] =
    ((PLUS | MINUS | TIMES | DIVIDE | AND | OR |
      opt(NOT) ~ EQUAL | GT | LT | GTE | LTE) ~ expression) | instanceof

  def instanceof: Parser[ Any ] =
    "instanceof" ~ jolieType
  def jolieType: Parser[ Any ] =
    ident
  def nativeType: Parser[ Any ] =
    ( INT_TYPE
      | LONG_TYPE
      | DOUBLE_TYPE
      | STRING_TYPE
      | RAW_TYPE
      | VOID_TYPE
      | ANY_TYPE
      )

  def terExpression: Parser[ Any ] = (
    ( LR ~ expression ~ RR )
      | ( NOT ~ expression )
      | ( opt( NOT ) ~ path )
      | increment
      | decrement
      | ( SHARP ~ path )
      | ( CARET ~ path )
      | ( opt( MINUS ) ~ ( wholeNumber | extInt | floatingPointNumber ) )
      | stringLiteral
      | ( opt( NOT ) ~ ( TRUE | FALSE ) )
      | ( opt( NOT ) ~ is_native )
      | castType
    )

  def increment: Parser[ Any ] =
    ( PPLUS | MMINUS ) ~ path
  def decrement: Parser[ Any ] =
    path ~ ( PPLUS | MMINUS )

  def castType: Parser[ Any ] =
    ( INT_TYPE | LONG_TYPE | STRING_TYPE | BOOL_TYPE ) ~ LR ~ expression ~ RR

  def is_native: Parser[ Any ] =
    ( "is_string"
      | "is_double"
      | "is_int"
      | "is_long"
      | "is_bool") ~ LR ~ expression ~ RR

  def extInt: Parser[ Any ] = wholeNumber ~ ( "e" | "E" ) ~ wholeNumber
}

object JolieParserObject extends JolieParser {
  def main( args: Array[ String ] ): Unit = {
    val src = Source.fromFile( args{ 0 }).mkString
      .replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","")
    val p = parseAll( program, src )
    println( "Parser's class: " + p.getClass )
    p match {
      case Success(result, next) => println( "Parsed: " + result)
      case e: Error => println( "Error: " + e )
      case f: Failure => println( "Failure: " + f )
      case _ => println( "default" )
    }
    print( "done" )
  }
}