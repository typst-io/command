package io.typst.command.kotlin

import io.typst.command.Argument
import io.typst.command.Command
import io.typst.command.Command.pair

fun <T> commandMap(
    vararg args: Pair<String, Command<out T>>,
): Command.Mapping<T> = Command.mapping(*args.map { (name, cmd) -> pair(name, cmd) }.toTypedArray())

fun <T> command(t: T): Command.Parser<T> = Command.argument { t }

inline fun <T> command(crossinline func: () -> T): Command.Parser<T> = Command.argument { func() }

inline fun <T, A> command(crossinline func: (A) -> T, argA: Argument<A>): Command.Parser<T> =
    Command.argument({ a -> func(a) }, argA)

inline fun <T, A, B> command(crossinline func: (A, B) -> T, argA: Argument<A>, argB: Argument<B>): Command.Parser<T> =
    Command.argument({ a, b -> func(a, b) }, argA, argB)

inline fun <T, A, B, C> command(
    crossinline func: (A, B, C) -> T,
    argA: Argument<A>,
    argB: Argument<B>,
    argC: Argument<C>,
): Command.Parser<T> =
    Command.argument({ a, b, c -> func(a, b, c) }, argA, argB, argC)

inline fun <T, A, B, C, D> command(
    crossinline func: (A, B, C, D) -> T,
    argA: Argument<A>,
    argB: Argument<B>,
    argC: Argument<C>,
    argD: Argument<D>,
): Command.Parser<T> = Command.argument({ a, b, c, d -> func(a, b, c, d) }, argA, argB, argC, argD)

inline fun <T, A, B, C, D, E> command(
    crossinline func: (A, B, C, D, E) -> T,
    argA: Argument<A>,
    argB: Argument<B>,
    argC: Argument<C>,
    argD: Argument<D>,
    argE: Argument<E>,
): Command.Parser<T> = Command.argument({ a, b, c, d, e -> func(a, b, c, d, e) }, argA, argB, argC, argD, argE)


inline fun <T, A, B, C, D, E, F> command(
    crossinline func: (A, B, C, D, E, F) -> T,
    argA: Argument<A>,
    argB: Argument<B>,
    argC: Argument<C>,
    argD: Argument<D>,
    argE: Argument<E>,
    argF: Argument<F>,
): Command.Parser<T> =
    Command.argument({ a, b, c, d, e, f -> func(a, b, c, d, e, f) }, argA, argB, argC, argD, argE, argF)

inline fun <T, A, B, C, D, E, F, G> command(
    crossinline func: (A, B, C, D, E, F, G) -> T,
    argA: Argument<A>,
    argB: Argument<B>,
    argC: Argument<C>,
    argD: Argument<D>,
    argE: Argument<E>,
    argF: Argument<F>,
    argG: Argument<G>,
): Command.Parser<T> =
    Command.argument({ a, b, c, d, e, f, g -> func(a, b, c, d, e, f, g) }, argA, argB, argC, argD, argE, argF, argG)
