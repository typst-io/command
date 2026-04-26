package io.typst.command.bukkit

import io.typst.command.Argument
import io.typst.command.Command
import io.typst.command.Command.pair
import io.typst.command.LangKey
import io.typst.command.MessageKey
import io.typst.command.StandardArguments.intArg
import io.typst.command.StandardArguments.strArg
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

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
            "/mycmd e a"
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
}