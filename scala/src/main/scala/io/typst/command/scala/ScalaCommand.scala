package io.typst.command.scala

import io.typst.command.Command.{Mapping, Parser}
import io.typst.command.{algebra}
import io.typst.command.{Argument, Command, StandardArguments}
import io.typst.command.algebra.Tuple2

import java.util.{Collections, Optional}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.OptionConverters.RichOption

trait ScalaCommand {
  implicit val strArg: Argument[String] = StandardArguments.strArg
  implicit val intArg: Argument[Int] = Argument.ofUnary("int", (s: String) => s.toIntOption.toJava, () => Collections.emptyList())
  implicit val longArg: Argument[Long] = Argument.ofUnary("long", (s: String) => s.toLongOption.toJava, () => Collections.emptyList())
  implicit val floatArg: Argument[Float] = Argument.ofUnary("float", (s: String) => s.toFloatOption.toJava, () => Collections.emptyList())
  implicit val doubleArg: Argument[Double] = Argument.ofUnary("double", (s: String) => s.toDoubleOption.toJava, () => Collections.emptyList())
  implicit val boolArg: Argument[Boolean] = Argument.ofUnary("bool", (s: String) => s.toBooleanOption.toJava, () => Collections.emptyList())
  implicit val strsArg: Argument[Seq[String]] = Argument.of(
    Collections.singletonList("strings"),
    (args: java.util.List[String]) => new Tuple2(Optional.of(args.asScala.toSeq), Collections.emptyList()),
    Collections.emptyList()
  )
}

object ScalaCommand extends ScalaCommand {
  def argument[A](x: => A): Parser[A] = Command.argument(() => x)

  def mapping[A](pairs: (String, Command[_ <: A])*): Mapping[A] = {
    val tcPairs: Array[Tuple2[String, Command[_ <: A]]] = Array.fill(pairs.size)(null)
    for ((pair, i) <- pairs.zipWithIndex) {
      tcPairs.update(i, new Tuple2(pair._1, pair._2))
    }
    Command.mapping(tcPairs: _*)
  }

  def argument[T, A](f: A => T)(implicit argument: Argument[A]): Parser[T] = Command.argument((a: A) => f(a), argument)

  def argument[T, A, B](f: (A, B) => T)(implicit argA: Argument[A], argB: Argument[B]): Parser[T] = Command.argument((a: A, b: B) => f(a, b), argA, argB)

  def argument[T, A, B, C](f: (A, B, C) => T)(implicit argA: Argument[A], argB: Argument[B], argC: Argument[C]): Parser[T] = Command.argument((a: A, b: B, c: C) => f(a, b, c), argA, argB, argC)

  def argument[T, A, B, C, D](f: (A, B, C, D) => T)(implicit argA: Argument[A], argB: Argument[B], argC: Argument[C], argD: Argument[D]): Parser[T] = Command.argument((a: A, b: B, c: C, d: D) => f(a, b, c, d), argA, argB, argC, argD)

  def argument[T, A, B, C, D, E](f: (A, B, C, D, E) => T)(implicit argA: Argument[A], argB: Argument[B], argC: Argument[C], argD: Argument[D], argE: Argument[E]): Parser[T] = Command.argument((a: A, b: B, c: C, d: D, e: E) => f(a, b, c, d, e), argA, argB, argC, argD, argE)

  def argument[T, A, B, C, D, E, F](func: (A, B, C, D, E, F) => T)(implicit argA: Argument[A], argB: Argument[B], argC: Argument[C], argD: Argument[D], argE: Argument[E], argF: Argument[F]): Parser[T] = Command.argument((a: A, b: B, c: C, d: D, e: E, f: F) => func(a, b, c, d, e, f), argA, argB, argC, argD, argE, argF)

  def argument[T, A, B, C, D, E, F, G](func: (A, B, C, D, E, F, G) => T)(implicit argA: Argument[A], argB: Argument[B], argC: Argument[C], argD: Argument[D], argE: Argument[E], argF: Argument[F], argG: Argument[G]): Parser[T] = Command.argument((a: A, b: B, c: C, d: D, e: E, f: F, g: G) => func(a, b, c, d, e, f, g), argA, argB, argC, argD, argE, argF, argG)
}
