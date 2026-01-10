package io.typst.command

import io.typst.command.Command.pair
import io.typst.command.StandardArguments.intArg
import io.typst.command.StandardArguments.strArg
import io.typst.command.algebra.Either
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class CommandTest {

    sealed interface MyCommand

    data class AddItem(val index: Int, val name: String?) : MyCommand
    data class RemoveItem(val index: Number) : MyCommand
    data class PageItem(val index: Int, val indexB: Int) : MyCommand
    data object OpenItemList : MyCommand
    data object FallbackItem : MyCommand
    data class FallbackArg(val value: Int) : MyCommand
    data class FallbackOptArg(val value: Int?) : MyCommand {
        constructor(value: Optional<Int>) : this(value.orElse(null))
    }
    data object ReloadCommand : MyCommand

    companion object {
        private val intTabArg = intArg.withTabCompletes { listOf("10", "20") }
        private val intTabArg2 = intArg.withTabCompletes { listOf("30", "40") }

        val pageCmd: Command.Parser<MyCommand> = Command.argument(::PageItem, intTabArg, intTabArg2)

        val itemCommand: Command.Mapping<MyCommand> = Command.mapping(
            pair("open", Command.present<MyCommand>(OpenItemList).withDescription("Opens the item list.")),
            pair("add", Command.argument(::AddItem, intArg, strArg)),
            pair("remove", Command.argument(::RemoveItem, intArg)),
            pair("page", pageCmd),
            pair("lazy", Command.present(AddItem(0, null))),
            pair("camelPage", pageCmd)
        )

        private val itemCommandWithFallback: Command.Mapping<MyCommand> =
            itemCommand.withFallback(Command.present(FallbackItem))

        val itemCommandWithFallback2: Command.Mapping<MyCommand> =
            itemCommandWithFallback.withFallback(Command.argument(::FallbackArg, intArg))

        val itemCommandWithFallback3: Command.Mapping<MyCommand> =
            itemCommandWithFallback.withFallback(Command.argument(::FallbackOptArg, intArg.asOptional()))

        private val reloadCommand: Command<MyCommand> =
            Command.present<MyCommand>(ReloadCommand).withDescription("Reloads the configuration.")

        val rootCommand: Command<MyCommand> = Command.mapping(
            pair("item", itemCommand),
            pair("reload", reloadCommand)
        )

        private fun left(failure: CommandFailure<MyCommand>): Either<CommandFailure<MyCommand>, CommandSuccess<MyCommand>> =
            Either.Left(failure)

        private fun right(success: CommandSuccess<MyCommand>): Either<CommandFailure<MyCommand>, CommandSuccess<MyCommand>> =
            Either.Right(success)
    }

    @Test
    fun `return FewArguments when no args provided`() {
        val args = emptyArray<String>()
        val result = Command.parse(args, rootCommand)

        assertThat(result).isEqualTo(left(CommandFailure.FewArguments(args, 0, rootCommand)))
    }

    @Test
    fun `return FewArguments for incomplete subcommand`() {
        val args = arrayOf("item")
        val result = Command.parse(args, rootCommand)

        assertThat(result).isEqualTo(left(CommandFailure.FewArguments(args, 1, itemCommand)))
    }

    @Test
    fun `return UnknownSubCommand for invalid subcommand`() {
        val args = arrayOf("item", "unknownCommand")
        val result = Command.parse(args, rootCommand)

        assertThat(result).isEqualTo(left(CommandFailure.UnknownSubCommand(args, 1, itemCommand)))
    }

    @Test
    fun `parse present command successfully`() {
        val args = arrayOf("item", "open")
        val result = Command.parse(args, rootCommand)

        assertThat(result).isEqualTo(
            right(CommandSuccess(args, 2, OpenItemList, itemCommand.commandMap["open"]))
        )
    }

    @Test
    fun `parse command with arguments`() {
        val index = 0
        val name = "someName"
        val args = arrayOf("item", "add", index.toString(), name)
        val result = Command.parse(args, rootCommand)

        assertThat(result).isEqualTo(
            right(CommandSuccess(args, args.size, AddItem(index, name), itemCommand.commandMap["add"]))
        )
    }

    @Test
    fun `use fallback when no subcommand matches`() {
        val args = arrayOf("item")
        val commandMap: Command.Mapping<MyCommand> = Command.mapping(
            pair("item", itemCommandWithFallback)
        )
        val result = Command.parse(args, commandMap)

        assertThat(result).isEqualTo(
            right(CommandSuccess(args, 1, FallbackItem, itemCommandWithFallback.fallback.orElse(null)))
        )
    }

    @Test
    fun `return UnknownSubCommand when fallback exists but input is invalid`() {
        val args = arrayOf("item", "help")
        val commandMap: Command.Mapping<MyCommand> = Command.mapping(
            pair("item", itemCommandWithFallback)
        )
        val result = Command.parse(args, commandMap)

        assertThat(result).isEqualTo(left(CommandFailure.UnknownSubCommand(args, 1, itemCommandWithFallback)))
    }

    @Test
    fun `parse fallback with argument`() {
        val args = arrayOf("item", "1")
        val commandMap: Command.Mapping<MyCommand> = Command.mapping(
            pair("item", itemCommandWithFallback2)
        )
        val result = Command.parse(args, commandMap)

        assertThat(result).isEqualTo(
            right(CommandSuccess(args, 2, FallbackArg(1), itemCommandWithFallback2.fallback.orElse(null)))
        )
    }

    @Test
    fun `parse fallback with optional argument`() {
        val args = arrayOf("item", "1")
        val commandMap: Command.Mapping<MyCommand> = Command.mapping(
            pair("item", itemCommandWithFallback3)
        )
        val result = Command.parse(args, commandMap)

        assertThat(result).isEqualTo(
            right(CommandSuccess(args, 2, FallbackOptArg(Optional.of(1)), itemCommandWithFallback3.fallback.orElse(null)))
        )
    }

    @Test
    fun `return UnknownSubCommand for invalid input with optional fallback`() {
        val args = arrayOf("item", "help")
        val commandMap: Command.Mapping<MyCommand> = Command.mapping(
            pair("item", itemCommandWithFallback3)
        )
        val result = Command.parse(args, commandMap)

        assertThat(result).isEqualTo(left(CommandFailure.UnknownSubCommand(args, 1, itemCommandWithFallback3)))
    }

    @Test
    fun `tab complete at root level`() {
        val args = emptyArray<String>()
        val result = Command.tabComplete(args, rootCommand)

        assertThat(result).isEqualTo(
            CommandTabResult.Suggestions(
                listOf(
                    pair("item", Optional.of(itemCommand)),
                    pair("reload", Optional.of(reloadCommand))
                )
            )
        )
    }

    @Test
    fun `tab complete subcommands`() {
        val args = arrayOf("item", "")
        val result = Command.tabComplete(args, rootCommand)

        assertThat(result).isEqualTo(
            CommandTabResult.Suggestions(
                listOf("open", "add", "remove", "page", "lazy", "camelPage").map {
                    pair(it, Optional.ofNullable(itemCommand.commandMap[it]))
                }
            )
        )
    }

    @Test
    fun `tab complete returns empty for unknown subcommand`() {
        val args = arrayOf("item", "unknownCommand")
        val result = Command.tabComplete(args, rootCommand)

        assertThat(result).isEqualTo(CommandTabResult.Suggestions<MyCommand>(emptyList()))
    }

    @Test
    fun `tab complete custom suggestions for first argument`() {
        assertThat(Command.tabComplete(arrayOf("item", "page", ""), rootCommand))
            .isEqualTo(
                CommandTabResult.Suggestions(
                    listOf("10", "20").map { pair(it, Optional.of<Command<MyCommand>>(pageCmd)) }
                )
            )

        assertThat(Command.tabComplete(arrayOf("item", "page", "1"), rootCommand))
            .isEqualTo(
                CommandTabResult.Suggestions(
                    listOf("10").map { pair(it, Optional.of<Command<MyCommand>>(pageCmd)) }
                )
            )
    }

    @Test
    fun `tab complete custom suggestions for second argument`() {
        assertThat(Command.tabComplete(arrayOf("item", "page", "10", ""), rootCommand))
            .isEqualTo(
                CommandTabResult.Suggestions(
                    listOf("30", "40").map { pair(it, Optional.of<Command<MyCommand>>(pageCmd)) }
                )
            )

        assertThat(Command.tabComplete(arrayOf("item", "page", "10", "3"), rootCommand))
            .isEqualTo(
                CommandTabResult.Suggestions(
                    listOf("30").map { pair(it, Optional.of<Command<MyCommand>>(pageCmd)) }
                )
            )
    }

    @Test
    fun `tab complete works with camelCase commands`() {
        assertThat(Command.tabComplete(arrayOf("item", "camelPage", "10", ""), rootCommand))
            .isEqualTo(
                CommandTabResult.Suggestions(
                    listOf("30", "40").map { pair(it, Optional.of<Command<MyCommand>>(pageCmd)) }
                )
            )
    }

    @Test
    fun `generate help from command failure`() {
        val args = arrayOf("item", "a")
        val result = Command.parse(args, rootCommand)

        val failure = (result as Either.Left).left
        assertThat(failure).isInstanceOf(CommandFailure.UnknownSubCommand::class.java)

        val unknown = failure as CommandFailure.UnknownSubCommand
        val entries = Command.getEntries(unknown.command)

        assertThat(entries).isNotEmpty
        assertThat(entries.map { it.key }).contains(listOf("open"))
    }
}