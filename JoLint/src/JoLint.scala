import scala.util.parsing.combinator.JavaTokenParsers

sealed trait Program
case class Main( pars: List[ Parallel ] ) extends Program
case class Parallel( seqs: List[Sequence] ) extends Program
case class Sequence( stats: List[ String ]) extends Program

class JolieParser extends JavaTokenParsers {

  val MAIN = "main"
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

  def program: Parser[ Program ] =
    mainProcedure

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

object ParseExpr extends JolieParser {
  def main( args: Array[ String ] ): Unit = {
    val src = scala.io.Source.fromFile( args{ 0 }).mkString
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

//object ParseExpr extends JolieParser {
//  def main( args: Array[ String ] ): Unit = {
//    val ln = "main { c = 5; c++; c = c instanceof string }"
//    println( "input: " + ln )
//    println( parseAll( mainProcedure, ln ) )
//  }
//}