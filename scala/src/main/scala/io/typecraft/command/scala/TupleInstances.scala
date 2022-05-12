package io.typecraft.command.scala

import io.vavr.API.Tuple

import scala.language.implicitConversions

trait TupleInstances {
  implicit def vavrTuple1FromScala[A](
      tup: Tuple1[A]
  ): io.vavr.Tuple1[A] =
    Tuple(tup._1)

  implicit def vavrTuple2FromScala[A, B](
      tup: Tuple2[A, B]
  ): io.vavr.Tuple2[A, B] =
    Tuple(tup._1, tup._2)

  implicit def vavrTuple3FromScala[A, B, C](
      tup: Tuple3[A, B, C]
  ): io.vavr.Tuple3[A, B, C] =
    Tuple(tup._1, tup._2, tup._3)
}
