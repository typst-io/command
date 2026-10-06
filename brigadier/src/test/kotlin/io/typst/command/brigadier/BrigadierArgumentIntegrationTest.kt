package io.typst.command.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.typst.command.Argument
import io.typst.command.Command
import io.typst.command.Command.pair
import io.typst.command.StandardArguments.boolArg
import io.typst.command.StandardArguments.intArg
import io.typst.command.StandardArguments.strArg
import io.typst.command.StandardArguments.strsArg
import io.typst.command.algebra.Tuple2
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import java.util.Locale
import java.util.Optional

class BrigadierArgumentIntegrationTest {

    private fun <A> register(command: Command<A>, results: MutableList<A>): CommandDispatcher<Unit> {
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("cmd", command) { _, result -> results.add(result) })
        return dispatcher
    }

    @Test
    fun `execute with any number of trailing optional arguments`() {
        val command = Command.argument(
            { count: Int, enabled: Optional<Boolean>, name: Optional<String> -> Triple(count, enabled, name) },
            intArg, boolArg.asOptional(), strArg.asOptional()
        )
        val results = mutableListOf<Triple<Int, Optional<Boolean>, Optional<String>>>()
        val dispatcher = register(command, results)

        dispatcher.execute("cmd 7", Unit)
        dispatcher.execute("cmd 7 true", Unit)
        dispatcher.execute("cmd 7 false diamond", Unit)

        assertThat(results).containsExactly(
            Triple(7, Optional.empty(), Optional.empty()),
            Triple(7, Optional.of(true), Optional.empty()),
            Triple(7, Optional.of(false), Optional.of("diamond"))
        )
    }

    @Test
    fun `reject invalid optional input instead of executing its omitted branch`() {
        val command = Command.argument({ value: Optional<Int> -> value }, intArg.asOptional())
        val results = mutableListOf<Optional<Int>>()
        val dispatcher = register(command, results)

        assertThatThrownBy { dispatcher.execute("cmd invalid", Unit) }
            .isInstanceOf(CommandSyntaxException::class.java)
        assertThat(results).isEmpty()
    }

    @Test
    fun `do not omit a required argument after an optional argument`() {
        val command = Command.argument(
            { number: Optional<Int>, name: String -> pair(number, name) }, intArg.asOptional(), strArg
        )
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("cmd", command) { _, _ -> })

        assertThatThrownBy { dispatcher.execute("cmd", Unit) }
            .isInstanceOf(CommandSyntaxException::class.java)
    }

    @Test
    fun `preserve optional behavior through argument mapping and withers`() {
        val argument = intArg.asOptional().map { value -> value.orElse(99) }
            .withName("amount").withTabCompletes { listOf("42") }
        val results = mutableListOf<Int>()
        val dispatcher = register(Command.argument({ value: Int -> value }, argument), results)

        dispatcher.execute("cmd", Unit)
        dispatcher.execute("cmd 42", Unit)

        assertThat(results).containsExactly(99, 42)
    }

    @Test
    fun `do not call user parsers or factories during registration and completion`() {
        val parsedInputs = mutableListOf<String>()
        val constructedValues = mutableListOf<Optional<Int>>()
        val argument = Argument.ofUnary("number", Int::class.javaObjectType, { input ->
            parsedInputs.add(input)
            Optional.of(input.toInt())
        }, { listOf("42") }).asOptional()
        val command = Command.argument({ value: Optional<Int> ->
            constructedValues.add(value)
            value
        }, argument)
        val results = mutableListOf<Optional<Int>>()
        val dispatcher = register(command, results)

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("cmd ", Unit)).join()

        assertThat(suggestions.list.map { it.text }).containsExactly("42")
        assertThat(parsedInputs).isEmpty()
        assertThat(constructedValues).isEmpty()
        assertThat(results).isEmpty()
    }

    @Test
    fun `preserve greedy behavior through argument mapping and withers`() {
        val argument = strsArg.map { values -> values.joinToString("|") }
            .withName("words").withTabCompletes { emptyList() }
        val results = mutableListOf<String>()
        val dispatcher = register(Command.argument({ value: String -> value }, argument), results)

        dispatcher.execute("cmd", Unit)
        dispatcher.execute("cmd alpha beta", Unit)

        assertThat(results).containsExactly("", "alpha|beta")
    }

    @Test
    fun `parse greedy tokens after a required argument and ignore repeated whitespace`() {
        val command = Command.argument({ count: Int, values: List<String> -> pair(count, values) }, intArg, strsArg)
        val results = mutableListOf<Tuple2<Int, List<String>>>()
        val dispatcher = register(command, results)

        dispatcher.execute("cmd 7", Unit)
        dispatcher.execute("cmd 7 alpha   한글:/value  ", Unit)

        assertThat(results).containsExactly(pair(7, emptyList()), pair(7, listOf("alpha", "한글:/value")))
    }

    @Test
    fun `keep greedy quotes as literal token content`() {
        val results = mutableListOf<List<String>>()
        val dispatcher = register(Command.argument({ values: List<String> -> values }, strsArg), results)

        dispatcher.execute("cmd \"alpha beta\" gamma", Unit)

        assertThat(results).containsExactly(listOf("\"alpha", "beta\"", "gamma"))
    }

    @Test
    fun `keep a quoted single string as one token`() {
        val results = mutableListOf<String>()
        val dispatcher = register(Command.argument({ value: String -> value }, strArg), results)

        dispatcher.execute("cmd \"alpha beta\"", Unit)

        assertThat(results).containsExactly("alpha beta")
    }

    @Test
    fun `a unary list result must still consume only one token`() {
        val argument = Argument.ofUnary("list", List::class.java, { input -> Optional.of(listOf(input)) }, { emptyList() })
        val results = mutableListOf<List<String>>()
        val dispatcher = register(Command.argument({ values: List<String> -> values }, argument), results)

        assertThatThrownBy { dispatcher.execute("cmd alpha beta", Unit) }
            .isInstanceOf(CommandSyntaxException::class.java)
        assertThat(results).isEmpty()
        dispatcher.execute("cmd alpha", Unit)
        assertThat(results).containsExactly(listOf("alpha"))
    }

    @Test
    fun `reject a greedy argument that is not last during registration`() {
        val command = Command.argument({ values: List<String>, count: Int -> pair(values, count) }, strsArg, intArg)

        assertThatThrownBy { BrigadierCommands.from("cmd", command) { _: Unit, _ -> } }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("last")
    }

    @Test
    fun `context includes subcommands decoded previous arguments and the current prefix`() {
        val capturedArgs = mutableListOf<List<String>>()
        val argument = strArg.withContextualTabCompleter { context ->
            capturedArgs.add(context.args)
            listOf("diamond", "gold")
        }
        val command = Command.mapping(pair("nested", Command.argument(
            { item: String, name: String -> pair(item, name) }, strArg, argument
        )))
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("cmd", command) { _, _ -> })

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("cmd nested \"some item\" DI", Unit)).join()

        assertThat(suggestions.list.map { it.text }).containsExactly("diamond")
        assertThat(capturedArgs).containsExactly(listOf("nested", "some item", "DI"))
    }

    @Test
    fun `context excludes input after the completion cursor`() {
        val capturedArgs = mutableListOf<List<String>>()
        val argument = strArg.withContextualTabCompleter { context ->
            capturedArgs.add(context.args)
            listOf("diamond", "gold")
        }
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("cmd", Command.argument({ value: String -> value }, argument)) { _, _ -> })
        val input = "cmd di ignored"

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(input, Unit), "cmd di".length).join()

        assertThat(suggestions.list.map { it.text }).containsExactly("diamond")
        assertThat(capturedArgs).containsExactly(listOf("di"))
    }

    @Test
    fun `complete individual greedy tokens while retaining preceding tokens`() {
        val capturedArgs = mutableListOf<List<String>>()
        val argument = strsArg.withContextualTabCompleter { context ->
            capturedArgs.add(context.args)
            listOf("beta", "gold")
        }
        val results = mutableListOf<List<String>>()
        val dispatcher = register(Command.argument({ values: List<String> -> values }, argument), results)
        val input = "cmd alpha be"

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(input, Unit)).join()

        assertThat(capturedArgs).containsExactly(listOf("alpha", "be"))
        assertThat(suggestions.list.map { it.apply(input) }).containsExactly("cmd alpha beta")
        assertThat(results).isEmpty()
    }

    @Test
    fun `execute named and optional fallback commands under a nested mapping`() {
        val fallback = Command.argument({ value: Optional<Int> -> value.orElse(7) }, intArg.asOptional())
        val command = Command.mapping(pair("nested", Command.mapping(pair("named", Command.present(1)))
            .withFallback(fallback)))
        val results = mutableListOf<Int>()
        val dispatcher = register(command, results)

        dispatcher.execute("cmd nested", Unit)
        dispatcher.execute("cmd nested 42", Unit)
        dispatcher.execute("cmd nested named", Unit)

        assertThat(results).containsExactly(7, 42, 1)
    }

    @Test
    fun `preserve the four argument contextual factory and its greedy list behavior`() {
        val argument = Argument.ofContext("legacy", List::class.java, strsArg.parser) { emptyList() }
        val results = mutableListOf<List<String>>()
        val dispatcher = register(Command.argument({ values: List<String> -> values }, argument), results)

        dispatcher.execute("cmd alpha beta", Unit)

        assertThat(results).containsExactly(listOf("alpha", "beta"))
    }

    @Test
    fun `complete quoted prefixes and quote suggestions containing spaces`() {
        val argument = strArg.withTabCompletes { listOf("diamond sword", "dirt", "gold") }
        for (input in listOf("cmd di", "cmd \"di", "cmd 'di", "cmd \"di\\")) {
            val results = mutableListOf<String>()
            val dispatcher = register(Command.argument({ value: String -> value }, argument), results)

            val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(input, Unit)).join()
            val completedInputs = suggestions.list.map { it.apply(input) }

            assertThat(completedInputs).containsExactly("cmd \"diamond sword\"", "cmd dirt")
            assertThat(results).isEmpty()
            completedInputs.forEach { dispatcher.execute(it, Unit) }
            assertThat(results).containsExactly("diamond sword", "dirt")
        }
    }

    @Test
    fun `decode escaped quotes in completion prefixes and escape the suggested value`() {
        val argument = strArg.withTabCompletes { listOf("dia\"mond", "diamond") }
        val results = mutableListOf<String>()
        val dispatcher = register(Command.argument({ value: String -> value }, argument), results)
        val input = "cmd \"dia\\\""

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(input, Unit)).join()

        assertThat(suggestions.list.map { it.text }).containsExactly("\"dia\\\"mond\"")
        dispatcher.execute(suggestions.list.single().apply(input), Unit)
        assertThat(results).containsExactly("dia\"mond")
    }

    @Test
    @ResourceLock("java.util.Locale.default")
    fun `filter suggestions independently of the JVM locale`() {
        val previousLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val argument = strArg.withTabCompletes { listOf("ITEM", "iron", "gold") }
            val dispatcher = register(Command.argument({ value: String -> value }, argument), mutableListOf())

            val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("cmd I", Unit)).join()

            assertThat(suggestions.list.map { it.text }).containsExactly("iron", "ITEM")
        } finally {
            Locale.setDefault(previousLocale)
        }
    }
}
