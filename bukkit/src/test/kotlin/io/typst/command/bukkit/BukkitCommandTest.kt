package io.typst.command.bukkit

import io.typst.command.Argument
import io.typst.command.Command
import io.typst.command.Command.pair
import io.typst.command.CommandCancellationException
import io.typst.command.LangKey
import io.typst.command.MessageKey
import io.typst.command.StandardArguments.intArg
import io.typst.command.StandardArguments.strArg
import net.md_5.bungee.api.chat.BaseComponent
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class BukkitCommandTest {

    companion object {
        private val strArgTab: Argument<String> = strArg.withTabCompletes { listOf("tab") }

        val command: Command.Mapping<Any?> = Command.mapping(
            pair("a", Command.present(null)),
            pair("b", Command.argument({ it }, strArg)),
            pair("c", Command.argument({ _, _ -> null }, strArg, intArg)),
            pair("d", Command.argument({ _, _ -> null }, strArg, intArg).withDescription("desc")),
            pair("e", Command.mapping<Any?>(
                pair("a", Command.present(null))
            )),
            pair("f", Command.argument({ _, _ -> null }, strArgTab, intArg)
                .withDescription("desc")
                .withPermission("test.permission"))
        ).withFallback(Command.argument({ it }, strArg))
    }

    private lateinit var player: Player
    private lateinit var messages: MutableList<String>

    @BeforeEach
    fun setUp() {
        player = mock(Player::class.java)
        messages = mutableListOf()

        `when`(player.locale).thenReturn("ko_kr")
        `when`(player.uniqueId).thenReturn(UUID.randomUUID())
        `when`(player.hasPermission(anyString())).thenReturn(false)
        val spigot = mock(Player.Spigot::class.java)
        `when`(player.spigot()).thenReturn(spigot)
        doAnswer { invocation ->
            messages.add(invocation.getArgument<BaseComponent>(0).toPlainText())
            null
        }.`when`(spigot).sendMessage(any(BaseComponent::class.java))
        doAnswer { invocation ->
            messages.add(ChatColor.stripColor(invocation.getArgument(0))!!)
            null
        }.`when`(player).sendMessage(anyString())
    }

    @Test
    fun `generate help messages`() {
        val msgs = BukkitCommands.getCommandUsages(
            player,
            "mycmd",
            emptyArray(),
            1,
            command,
            BukkitCommandConfig.empty
        ).map { ChatColor.stripColor(it) }

        assertThat(msgs).containsExactly(
            "/mycmd a",
            "/mycmd b (문자열)",
            "/mycmd c (문자열) (정수)",
            "/mycmd d (문자열) (정수) - desc",
            "/mycmd e a",
            "/mycmd (문자열)"
        )
    }

    @Test
    fun `generate help for fallback command`() {
        val msgs = BukkitCommands.getCommandUsages(
            player,
            "mycmd",
            emptyArray(),
            1,
            command.fallback.orElse(null),
            BukkitCommandConfig.empty
        ).map { ChatColor.stripColor(it) }

        assertThat(msgs).containsExactly("/mycmd (문자열)")
    }

    @Test
    fun `show error message for incomplete command`() {
        BukkitCommands.execute(player, "mycmd", arrayOf("d"), command, BukkitCommandConfig.empty)

        val output = messages.joinToString("\n")
        assertThat(output).contains("/mycmd d (문자열) (정수) - desc")
        assertThat(output).contains("잘못된 명령어입니다!")
    }

    @Test
    fun `hide tab complete for argument without permission`() {
        val completes = BukkitCommands.tabComplete(
            player,
            arrayOf("f", ""),
            command
        ) { _, _ -> emptyList() }

        assertThat(completes).isEmpty()
    }

    @Test
    fun `custom error message`() {
        val msg = "명령어 오사용"
        val config = BukkitCommandConfig.empty
            .withMessage(LangKey.KOREAN, MessageKey.INVALID_COMMAND, msg)
        BukkitCommands.execute(player, "mycmd", arrayOf("d"), command, config)

        val output = messages.joinToString("\n")
        assertThat(output).contains("/mycmd d (문자열) (정수) - desc")
        assertThat(output).contains(msg)
    }

    @Test
    fun `deny permission before invoking an argument parser`() {
        val sender = mock(CommandSender::class.java)
        val parsedInputs = mutableListOf<String>()
        val argument = Argument.ofUnary("externalLookup", String::class.java, { input ->
            parsedInputs.add(input)
            Optional.of(input)
        }, { emptyList() })
        val command = Command.argument({ input: String -> input }, argument)
            .withPermission("admin.lookup")

        assertThatThrownBy {
            BukkitCommands.execute(sender, "lookup", arrayOf("untrusted"), command, BukkitCommandConfig.empty)
        }.isInstanceOf(CommandCancellationException::class.java)

        assertThat(parsedInputs).describedAs("Unauthorized input must not reach the protected parser").isEmpty()
    }

    @Test
    fun `deny permission before invoking an argument tab completer`() {
        val sender = mock(CommandSender::class.java)
        val queriedContexts = mutableListOf<List<String>>()
        val argument = intArg.withContextualTabCompleter { context ->
            queriedContexts.add(context.args)
            listOf("42")
        }
        val command = Command.argument({ value: Int -> value }, argument).withPermission("admin.lookup")

        val suggestions = BukkitCommands.tabComplete(sender, arrayOf(""), command) { _, _ -> emptyList() }

        assertThat(suggestions).isEmpty()
        assertThat(queriedContexts).describedAs("Unauthorized completion must not invoke the protected lookup").isEmpty()
    }

    @Test
    fun `invoke the custom tab completer after declared arguments have been parsed`() {
        val command = Command.argument({ value: Int -> value }, intArg)

        val suggestions = BukkitCommands.tabComplete(
            mock(CommandSender::class.java), arrayOf("42", ""), command
        ) { _, value -> listOf("value-$value") }

        assertThat(suggestions).containsExactly("value-42")
    }

    @Test
    fun `do not repeat a parsed prefix in nested command help`() {
        val command = Command.mapping(pair("branch", Command.mapping(pair("leaf", Command.present("leaf")))))

        val usages = BukkitCommands.getCommandUsages(
            mock(CommandSender::class.java), "cmd", arrayOf("root", "unknown"), 1,
            command, BukkitCommandConfig.empty
        ).map { ChatColor.stripColor(it) }

        assertThat(usages).containsExactly("/cmd root branch leaf")
    }

    @Test
    fun `include the fallback command in generated help`() {
        val command = Command.mapping(pair("named", Command.present(0)))
            .withFallback(Command.argument({ value: Int -> value }, intArg))

        val usages = BukkitCommands.getCommandUsages(
            mock(CommandSender::class.java), "cmd", emptyArray(), 0, command, BukkitCommandConfig.empty
        ).map { ChatColor.stripColor(it) }

        assertThat(usages).containsExactlyInAnyOrder("/cmd named", "/cmd (int)")
    }

    @Test
    fun `show restricted command help when hiding is disabled`() {
        val command = Command.mapping(pair("admin", Command.present("admin").withPermission("admin.command")))
        val config = BukkitCommandConfig.empty.withHideNoPermissionCommands(false)

        val usages = BukkitCommands.getCommandUsages(
            mock(CommandSender::class.java), "cmd", emptyArray(), 0, command, config
        ).map { ChatColor.stripColor(it) }

        assertThat(usages).containsExactly("/cmd admin")
    }

    @Test
    fun `preserve explicit YAML null values during normalization`() {
        val yamlValues = linkedMapOf<String, Any?>("unset" to null)

        val normalized = BukkitConverters.normalizeYamlMap(yamlValues)

        assertThat(normalized).containsKey("unset")
        assertThat(normalized["unset"]).isNull()
    }
}
