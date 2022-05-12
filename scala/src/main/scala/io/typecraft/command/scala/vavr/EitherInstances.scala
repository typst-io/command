package io.typecraft.command.scala.vavr

trait EitherInstances {
  implicit class ToEitherOps[A, B](either: io.vavr.control.Either[A, B]) {
    def toScala: Either[A, B] =
      if (either.isRight) {
        Right(either.get())
      } else {
        Left(either.getLeft)
      }
  }
}
