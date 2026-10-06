package io.typst.command.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.typst.command.Command
import io.typst.command.Command.pair
import io.typst.command.StandardArguments.*
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BrigadierCommandsTest {

    private lateinit var dispatcher: CommandDispatcher<TestSource>
    private lateinit var executedCommands: MutableList<Any>

    sealed interface ItemCommand
    data class AddItem(val index: Int, val name: String) : ItemCommand
    data class RemoveItem(val index: Int) : ItemCommand
    data object OpenItem : ItemCommand
    data class ToggleCommand(val enabled: Boolean) : ItemCommand
    data class PriceCommand(val price: Double) : ItemCommand
    data class MaterialCommand(val material: String) : ItemCommand

    data class TestSource(val name: String)

    @BeforeEach
    fun setUp() {
        dispatcher = CommandDispatcher()
        executedCommands = mutableListOf()
    }

    @Test
    fun `execute present command`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("open", Command.present(OpenItem))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        dispatcher.execute("item open", TestSource("player"))

        assertThat(executedCommands).containsExactly(OpenItem)
    }

    @Test
    fun `parse single int argument`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("remove", Command.argument(::RemoveItem, intArg))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        dispatcher.execute("item remove 42", TestSource("player"))

        assertThat(executedCommands).containsExactly(RemoveItem(42))
    }

    @Test
    fun `parse multiple arguments`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("add", Command.argument(::AddItem, intArg, strArg))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        dispatcher.execute("item add 10 diamond", TestSource("player"))

        assertThat(executedCommands).containsExactly(AddItem(10, "diamond"))
    }

    @Test
    fun `execute multiple subcommands`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("add", Command.argument(::AddItem, intArg, strArg)),
            pair("remove", Command.argument(::RemoveItem, intArg)),
            pair("open", Command.present(OpenItem))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        dispatcher.execute("item add 5 sword", TestSource("player"))
        dispatcher.execute("item remove 3", TestSource("player"))
        dispatcher.execute("item open", TestSource("player"))

        assertThat(executedCommands).containsExactly(
            AddItem(5, "sword"),
            RemoveItem(3),
            OpenItem
        )
    }

    @Test
    fun `pass source to executor`() {
        val capturedSources = mutableListOf<TestSource>()
        val command: Command<ItemCommand> = Command.mapping(
            pair("open", Command.present(OpenItem))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { source, _ ->
            capturedSources.add(source)
        })

        val expectedSource = TestSource("testPlayer")
        dispatcher.execute("item open", expectedSource)

        assertThat(capturedSources).containsExactly(expectedSource)
        assertThat(capturedSources[0]).isSameAs(expectedSource)
    }

    @Test
    fun `throw exception for unknown subcommand`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("open", Command.present(OpenItem))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        assertThatThrownBy { dispatcher.execute("item unknown", TestSource("player")) }
            .isInstanceOf(CommandSyntaxException::class.java)
    }

    @Test
    fun `handle nested mapping structure`() {
        val itemSubCommand: Command<ItemCommand> = Command.mapping(
            pair("add", Command.argument(::AddItem, intArg, strArg)),
            pair("remove", Command.argument(::RemoveItem, intArg))
        )
        val shopCommand: Command<ItemCommand> = Command.mapping(
            pair("item", itemSubCommand)
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("shop", shopCommand) { _, result ->
            executedCommands.add(result)
        })

        dispatcher.execute("shop item add 100 emerald", TestSource("player"))

        assertThat(executedCommands).containsExactly(AddItem(100, "emerald"))
    }

    @Test
    fun `parse boolean argument`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("toggle", Command.argument(::ToggleCommand, boolArg))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        dispatcher.execute("item toggle true", TestSource("player"))

        assertThat(executedCommands).containsExactly(ToggleCommand(true))
    }

    @Test
    fun `parse double argument`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("price", Command.argument(::PriceCommand, doubleArg))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        dispatcher.execute("item price 99.99", TestSource("player"))

        assertThat(executedCommands).containsExactly(PriceCommand(99.99))
    }

    @Test
    fun `tab completion for boolean argument`() {
        val command: Command<ItemCommand> = Command.mapping(
            pair("toggle", Command.argument(::ToggleCommand, boolArg))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        val suggestions = dispatcher.getCompletionSuggestions(
            dispatcher.parse("item toggle ", TestSource("player"))
        ).join()

        assertThat(suggestions.list.map { it.text }).containsExactlyInAnyOrder("true", "false")
    }

    @Test
    fun `tab completion for custom argument`() {
        val customArg = io.typst.command.Argument.ofUnary(
            "material",
            String::class.java,
            { java.util.Optional.of(it) },
            { listOf("diamond", "emerald", "gold", "iron") }
        )

        val command: Command<ItemCommand> = Command.mapping(
            pair("give", Command.argument(::MaterialCommand, customArg))
        )
        dispatcher.register(BrigadierCommands.from<TestSource, ItemCommand>("item", command) { _, result ->
            executedCommands.add(result)
        })

        val suggestions = dispatcher.getCompletionSuggestions(
            dispatcher.parse("item give ", TestSource("player"))
        ).join()

        assertThat(suggestions.list.map { it.text })
            .containsExactlyInAnyOrder("diamond", "emerald", "gold", "iron")
    }

    @Test
    fun `allow an optional argument to be omitted`() {
        val command = Command.argument({ value: Optional<Int> -> value }, intArg.asOptional())
        assertThat(Command.parseO(emptyArray(), command)).contains(Optional.empty())
        val results = mutableListOf<Optional<Int>>()
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("optional", command) { _, result ->
            results.add(result)
        })

        dispatcher.execute("optional", Unit)

        assertThat(results).containsExactly(Optional.empty<Int>())
    }

    @Test
    fun `preserve token boundaries for a greedy strings argument`() {
        val command = Command.argument({ values: List<String> -> values }, strsArg)
        assertThat(Command.parseO(arrayOf("alpha", "beta"), command)).contains(listOf("alpha", "beta"))
        val results = mutableListOf<List<String>>()
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("echo", command) { _, result ->
            results.add(result)
        })

        dispatcher.execute("echo alpha beta", Unit)

        assertThat(results).containsExactly(listOf("alpha", "beta"))
    }

    @Test
    fun `allow a greedy strings argument to consume zero tokens`() {
        val command = Command.argument({ values: List<String> -> values }, strsArg)
        assertThat(Command.parseO(emptyArray(), command)).contains(emptyList())
        val results = mutableListOf<List<String>>()
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("echo", command) { _, result ->
            results.add(result)
        })

        dispatcher.execute("echo", Unit)

        assertThat(results).containsExactly(emptyList())
    }

    @Test
    fun `filter argument suggestions by the typed prefix`() {
        val argument = strArg.withTabCompletes { listOf("diamond", "dirt", "gold") }
        val command = Command.argument({ value: String -> value }, argument)
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("give", command) { _, _ -> })

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("give di", Unit)).join()

        assertThat(suggestions.list.map { it.text }).containsExactly("diamond", "dirt")
    }

    @Test
    fun `pass preceding arguments to a contextual tab completer`() {
        val contextualArgument = strArg.withContextualTabCompleter { context ->
            if (context.args.contains("diamond")) listOf("sharpness") else emptyList()
        }
        val command = Command.argument({ item: String, enchantment: String -> "$item:$enchantment" }, strArg, contextualArgument)
        val results = mutableListOf<String>()
        val dispatcher = CommandDispatcher<Unit>()
        dispatcher.register(BrigadierCommands.from("enchant", command) { _, result -> results.add(result) })

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("enchant diamond ", Unit)).join()

        assertThat(suggestions.list.map { it.text }).containsExactly("sharpness")
        assertThat(results).isEmpty()
    }
}
