package io.typst.command

import io.typst.command.StandardArguments.intArg
import io.typst.command.algebra.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CommandArgumentTest {

    private fun <A : Tuple> assertNode(args: Array<String>, expected: A, node: Command.Parser<A>) {
        val arity = expected.arity()
        assertThat(args.size).isEqualTo(arity)
        assertThat(node.arguments.size).isEqualTo(arity)
        assertThat(Command.parse(args, node))
            .isEqualTo(Either.Right<CommandFailure<A>, CommandSuccess<A>>(CommandSuccess(args, arity, expected, node)))
    }

    @Test
    fun `parse 1 argument`() {
        assertNode(
            arrayOf("1"),
            Tuple1(1),
            Command.argument(::Tuple1, intArg)
        )
    }

    @Test
    fun `parse 2 arguments`() {
        assertNode(
            arrayOf("1", "2"),
            Tuple2(1, 2),
            Command.argument(::Tuple2, intArg, intArg)
        )
    }

    @Test
    fun `parse 3 arguments`() {
        assertNode(
            arrayOf("1", "2", "3"),
            Tuple3(1, 2, 3),
            Command.argument(::Tuple3, intArg, intArg, intArg)
        )
    }

    @Test
    fun `parse 4 arguments`() {
        assertNode(
            arrayOf("1", "2", "3", "4"),
            Tuple4(1, 2, 3, 4),
            Command.argument(::Tuple4, intArg, intArg, intArg, intArg)
        )
    }

    @Test
    fun `parse 5 arguments`() {
        assertNode(
            arrayOf("1", "2", "3", "4", "5"),
            Tuple5(1, 2, 3, 4, 5),
            Command.argument(::Tuple5, intArg, intArg, intArg, intArg, intArg)
        )
    }

    @Test
    fun `parse 6 arguments`() {
        assertNode(
            arrayOf("1", "2", "3", "4", "5", "6"),
            Tuple6(1, 2, 3, 4, 5, 6),
            Command.argument(::Tuple6, intArg, intArg, intArg, intArg, intArg, intArg)
        )
    }

    @Test
    fun `parse 7 arguments`() {
        assertNode(
            arrayOf("1", "2", "3", "4", "5", "6", "7"),
            Tuple7(1, 2, 3, 4, 5, 6, 7),
            Command.argument(::Tuple7, intArg, intArg, intArg, intArg, intArg, intArg, intArg)
        )
    }
}