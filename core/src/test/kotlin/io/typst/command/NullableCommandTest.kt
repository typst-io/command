package io.typst.command

import io.typst.command.Command.pair
import io.typst.command.StandardArguments.intArg
import io.typst.command.algebra.Either
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NullableCommandTest {
    private fun commands(onParsed: (List<Int>) -> Unit): List<Command.Parser<Void?>> = listOf(
        Command.argument({ a: Int, b: Int -> onParsed(listOf(a, b)); null }, intArg, intArg),
        Command.argument({ a: Int, b: Int, c: Int -> onParsed(listOf(a, b, c)); null }, intArg, intArg, intArg),
        Command.argument({ a: Int, b: Int, c: Int, d: Int -> onParsed(listOf(a, b, c, d)); null },
            intArg, intArg, intArg, intArg),
        Command.argument({ a: Int, b: Int, c: Int, d: Int, e: Int -> onParsed(listOf(a, b, c, d, e)); null },
            intArg, intArg, intArg, intArg, intArg),
        Command.argument({ a: Int, b: Int, c: Int, d: Int, e: Int, f: Int -> onParsed(listOf(a, b, c, d, e, f)); null },
            intArg, intArg, intArg, intArg, intArg, intArg),
        Command.argument({ a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int ->
            onParsed(listOf(a, b, c, d, e, f, g)); null }, intArg, intArg, intArg, intArg, intArg, intArg, intArg)
    )

    @Test
    fun `accept null results for every multi argument arity and retain the consumed index`() {
        val parsedValues = mutableListOf<List<Int>>()
        for (node in commands { parsedValues.add(it) }) {
            val values = (1..node.arguments.size).toList()
            val args = arrayOf("run", *values.map { it.toString() }.toTypedArray(), "remaining")
            val command = Command.mapping(pair("run", node))

            val result = Command.parse(args, command)

            assertThat(result).isEqualTo(Either.Right<CommandFailure<Void?>, CommandSuccess<Void?>>(
                CommandSuccess(args, values.size + 1, null, node)
            ))
            assertThat(parsedValues.last()).isEqualTo(values)
        }
        assertThat(parsedValues).hasSize(6)
    }

    @Test
    fun `skip result factories when any argument is invalid or missing`() {
        val parsedValues = mutableListOf<List<Int>>()
        for (node in commands { parsedValues.add(it) }) {
            val args = (1..node.arguments.size).map { it.toString() }
            for (position in args.indices) {
                val invalidArgs = args.mapIndexed { index, token -> if (index == position) "invalid" else token }

                assertThat(Command.parse(invalidArgs.toTypedArray(), node)).isInstanceOf(Either.Left::class.java)
                assertThat(Command.parse(args.take(position).toTypedArray(), node)).isInstanceOf(Either.Left::class.java)
            }
        }
        assertThat(parsedValues).isEmpty()
    }

    @Test
    fun `map a successful null result for every multi argument arity`() {
        for (node in commands {}) {
            val args = (1..node.arguments.size).map { it.toString() }.toTypedArray()
            val command = node.map { value -> if (value == null) "mapped" else "unexpected" }

            assertThat(Command.parseO(args, command)).contains("mapped")
        }
    }
}
