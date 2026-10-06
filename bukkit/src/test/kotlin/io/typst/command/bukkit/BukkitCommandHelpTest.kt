package io.typst.command.bukkit

import io.typst.command.Command
import io.typst.command.Command.pair
import io.typst.command.LangKey
import io.typst.command.MessageKey
import io.typst.command.StandardArguments.intArg
import io.typst.command.StandardArguments.strArg
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.chat.ComponentSerializer
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class BukkitCommandHelpTest {
    private val player = mock(Player::class.java)
    private val spigot = mock(Player.Spigot::class.java)
    private val components = mutableListOf<BaseComponent>()
    private val messages = mutableListOf<String>()

    @BeforeEach
    fun setUp() {
        `when`(player.locale).thenReturn("ko_kr")
        `when`(player.spigot()).thenReturn(spigot)
        doAnswer { invocation ->
            components.add(invocation.getArgument(0))
            null
        }.`when`(spigot).sendMessage(any(BaseComponent::class.java))
        doAnswer { invocation ->
            messages.add(invocation.getArgument(0))
            null
        }.`when`(player).sendMessage(anyString())
    }

    @Test
    fun `suggest nested command paths using the invoked alias and preserve help formatting`() {
        val command = Command.mapping(
            pair("item", Command.mapping(pair("add", Command.argument({ name: String -> name }, strArg)
                .withDescription("아이템 추가")))),
            pair("reload", Command.present("reload"))
        )

        val result = BukkitCommands.execute(player, "alias", emptyArray(), command, BukkitCommandConfig.empty)

        assertThat(result).isEmpty()
        assertThat(components.map { it.toPlainText() }).containsExactly(
            "/alias item add (문자열) - 아이템 추가", "/alias reload"
        )
        assertThat(components.map { it.clickEvent.action }).containsOnly(ClickEvent.Action.SUGGEST_COMMAND)
        assertThat(components.map { it.clickEvent.value }).containsExactly("/alias item add ", "/alias reload")
        assertThat(components.first().toLegacyText()).contains("${ChatColor.GREEN}/alias item add ",
            "${ChatColor.YELLOW}(문자열)", "${ChatColor.WHITE}- 아이템 추가")
    }

    @Test
    fun `suggest the parsed prefix once for nested fallback help`() {
        val command = Command.mapping(pair("branch", Command.mapping(pair("leaf", Command.present(0)))
            .withFallback(Command.argument({ value: Int -> value }, intArg))))

        BukkitCommands.execute(player, "cmd", arrayOf("branch", "invalid"), command, BukkitCommandConfig.empty)

        assertThat(components.map { it.toPlainText() }).containsExactly("/cmd branch leaf", "/cmd branch (정수)")
        assertThat(components.map { it.clickEvent.value }).containsExactly("/cmd branch leaf", "/cmd branch ")
        assertThat(messages).hasSize(2)
        assertThat(messages.last()).contains("invalid")
    }

    @Test
    fun `keep suggestions independent of custom help text and omit empty formatted entries`() {
        val command = Command.mapping(pair("hidden", Command.present(0)), pair("shown", Command.present(1)))
        val config = BukkitCommandConfig.empty.withFormatter { help ->
            if (help.arguments == listOf("hidden")) "" else "§b도움말"
        }

        BukkitCommands.execute(player, "cmd", emptyArray(), command, config)

        assertThat(components.map { it.toPlainText() }).containsExactly("도움말")
        assertThat(components.single().clickEvent.value).isEqualTo("/cmd shown")
    }

    @Test
    fun `suggest the root command for missing arguments`() {
        val command = Command.argument({ value: Int -> value }, intArg)

        BukkitCommands.execute(player, "cmd", emptyArray(), command, BukkitCommandConfig.empty)

        assertThat(components.map { it.toPlainText() }).containsExactly("/cmd (정수)")
        assertThat(components.single().clickEvent.value).isEqualTo("/cmd ")
    }

    @Test
    fun `send clickable help after an argument parsing failure and keep the error message plain`() {
        val command = Command.mapping(pair("item", Command.mapping(pair("add",
            Command.argument({ value: Int -> value }, intArg)))))
        val config = BukkitCommandConfig.empty.withMessage(LangKey.KOREAN, MessageKey.INVALID_COMMAND, "입력 오류")

        BukkitCommands.execute(player, "plugin:alias", arrayOf("item", "add", "oops"), command, config)

        assertThat(components.map { it.toPlainText() }).containsExactly("/plugin:alias item add (정수)")
        assertThat(components.single().clickEvent.value).isEqualTo("/plugin:alias item add ")
        assertThat(ComponentSerializer.toString(components.single())).contains(
            "\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/plugin:alias item add \"}"
        )
        assertThat(messages).containsExactly(" ", "입력 오류")
    }

    @Test
    fun `apply the permission visibility policy before formatting clickable help`() {
        val command = Command.mapping(
            pair("admin", Command.present(0).withPermission("admin.command")),
            pair("public", Command.present(1))
        )
        val formattedPaths = mutableListOf<List<String>>()
        val config = BukkitCommandConfig.empty.withFormatter { help ->
            formattedPaths.add(help.arguments)
            BukkitCommandHelp.format(help)
        }

        BukkitCommands.execute(player, "cmd", emptyArray(), command, config)

        assertThat(formattedPaths).containsExactly(listOf("public"))
        assertThat(components.map { it.clickEvent.value }).containsExactly("/cmd public")

        BukkitCommands.execute(player, "cmd", emptyArray(), command, config.withHideNoPermissionCommands(false))

        assertThat(formattedPaths).containsExactly(listOf("public"), listOf("admin"), listOf("public"))
        assertThat(components.map { it.clickEvent.value }).containsExactly("/cmd public", "/cmd admin", "/cmd public")
    }

    @Test
    fun `keep console help as plain text`() {
        val sender = mock(CommandSender::class.java)
        val output = mutableListOf<String>()
        doAnswer { invocation ->
            output.add(invocation.getArgument(0))
            null
        }.`when`(sender).sendMessage(anyString())
        val command = Command.mapping(pair("reload", Command.present(0)))
        val config = BukkitCommandConfig.empty.withFormatter { "console-help" }

        BukkitCommands.execute(sender, "cmd", emptyArray(), command, config)

        assertThat(output).containsExactly(" ", "console-help")
    }

    @Test
    fun `send no help or suggestion when parsing succeeds`() {
        val command = Command.mapping(pair("run", Command.present(42)))

        val result = BukkitCommands.execute(player, "cmd", arrayOf("run"), command, BukkitCommandConfig.empty)

        assertThat(result.get().command).isEqualTo(42)
        assertThat(components).isEmpty()
        assertThat(messages).isEmpty()
    }
}
