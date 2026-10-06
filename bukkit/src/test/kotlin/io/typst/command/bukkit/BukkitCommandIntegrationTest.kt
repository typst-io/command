package io.typst.command.bukkit

import io.typst.command.Argument
import io.typst.command.Command
import io.typst.command.Command.pair
import io.typst.command.CommandCancellationException
import io.typst.command.Converters
import io.typst.command.StandardArguments.intArg
import io.typst.command.StandardArguments.strArg
import io.typst.command.StandardArguments.strsArg
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bukkit.ChatColor
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.util.Optional

class BukkitCommandIntegrationTest {

    @Test
    fun `authorized execution parses once and preserves the original success node`() {
        val inputs = mutableListOf<String>()
        val argument = Argument.ofUnary("lookup", String::class.java, { input ->
            inputs.add(input)
            Optional.of(input)
        }, { emptyList() })
        val command = Command.argument({ value: String -> value }, argument).withPermission("admin.lookup")
        val sender = mock(CommandSender::class.java)
        `when`(sender.hasPermission("admin.lookup")).thenReturn(true)

        val result = BukkitCommands.execute(sender, "cmd", arrayOf("value"), command, BukkitCommandConfig.empty).get()

        assertThat(inputs).containsExactly("value")
        assertThat(result.command).isEqualTo("value")
        assertThat(result.node).isSameAs(command)
    }

    @Test
    fun `deny nested commands before their parser and factory run`() {
        val calls = mutableListOf<String>()
        val argument = Argument.ofUnary("lookup", String::class.java, { value ->
            calls.add("parser")
            Optional.of(value)
        }, { emptyList() })
        val protected = Command.argument({ value: String -> calls.add("factory"); value }, argument)
            .withPermission("admin.lookup")
        val command = Command.mapping(pair("nested", Command.mapping(pair("admin", protected))))

        assertThatThrownBy {
            BukkitCommands.execute(mock(CommandSender::class.java), "cmd", arrayOf("nested", "admin", "value"),
                command, BukkitCommandConfig.empty)
        }.isInstanceOf(CommandCancellationException::class.java)
        assertThat(calls).isEmpty()
    }

    @Test
    fun `deny an omitted fallback before its result factory runs`() {
        val calls = mutableListOf<String>()
        val fallback = Command.argument { calls.add("factory"); 42 }.withPermission("admin.lookup")
        val command = Command.mapping(pair("named", Command.present(1))).withFallback(fallback)

        assertThatThrownBy {
            BukkitCommands.execute(mock(CommandSender::class.java), "cmd", emptyArray(), command, BukkitCommandConfig.empty)
        }.isInstanceOf(CommandCancellationException::class.java)
        assertThat(calls).isEmpty()
    }

    @Test
    fun `deny completed arguments before parsing or invoking custom completion`() {
        val calls = mutableListOf<String>()
        val argument = Argument.ofUnary("lookup", String::class.java, { value ->
            calls.add("parser")
            Optional.of(value)
        }, { emptyList() })
        val command = Command.argument({ value: String -> calls.add("factory"); value }, argument)
            .withPermission("admin.lookup")

        val result = BukkitCommands.tabComplete(mock(CommandSender::class.java), arrayOf("value", ""), command) { _, _ ->
            calls.add("completion")
            listOf("secret")
        }

        assertThat(result).isEmpty()
        assertThat(calls).isEmpty()
    }

    @Test
    fun `a denied named command cannot fall through to an allowed fallback completer`() {
        val calls = mutableListOf<String>()
        val fallback = Command.argument({ value: String -> value }, strArg.withTabCompletes {
            calls.add("fallback")
            listOf("value")
        })
        val command = Command.mapping(pair("admin", Command.argument({ value: String -> value }, strArg)
            .withPermission("admin.lookup"))).withFallback(fallback)

        val result = BukkitCommands.tabComplete(mock(CommandSender::class.java), arrayOf("admin", ""), command) { _, _ ->
            calls.add("custom")
            emptyList()
        }

        assertThat(result).isEmpty()
        assertThat(calls).isEmpty()
    }

    @Test
    fun `keep permitted sibling suggestions when a fallback is denied`() {
        val calls = mutableListOf<String>()
        val fallback = Command.argument({ value: String -> value }, strArg.withTabCompletes {
            calls.add("fallback")
            listOf("secret")
        }).withPermission("admin.lookup")
        val command = Command.mapping(pair("public", Command.present("public")),
            pair("admin", Command.present("admin").withPermission("admin.lookup"))).withFallback(fallback)

        val result = BukkitCommands.tabComplete(mock(CommandSender::class.java), arrayOf(""), command) { _, _ -> emptyList() }

        assertThat(result).containsExactly("public")
        assertThat(calls).isEmpty()
    }

    @Test
    fun `invoke custom completion with a parsed nested fallback result`() {
        val seen = mutableListOf<Int>()
        val fallback = Command.argument({ value: Int -> value }, intArg).withPermission("lookup")
        val command = Command.mapping(pair("nested", Command.mapping(pair("named", Command.present(0))).withFallback(fallback)))
        val sender = mock(CommandSender::class.java)
        `when`(sender.hasPermission("lookup")).thenReturn(true)

        val result = BukkitCommands.tabComplete(sender, arrayOf("nested", "42", "value-"), command) { _, value ->
            seen.add(value)
            listOf("value-$value")
        }

        assertThat(result).containsExactly("value-42")
        assertThat(seen).containsExactly(42)
    }

    @Test
    fun `complete a current partial argument without parsing or constructing a command`() {
        val calls = mutableListOf<String>()
        val argument = Argument.ofUnary("number", Int::class.javaObjectType, { value ->
            calls.add("parser")
            Optional.of(value.toInt())
        }, { listOf("42", "99") })
        val command = Command.argument({ value: Int -> calls.add("factory"); value }, argument)

        val result = BukkitCommands.tabComplete(mock(CommandSender::class.java), arrayOf("4"), command) { _, _ ->
            calls.add("custom")
            emptyList()
        }

        assertThat(result).containsExactly("42")
        assertThat(calls).isEmpty()
    }

    @Test
    fun `invalid completed arguments do not invoke custom completion`() {
        var calls = 0
        val command = Command.argument({ value: Int -> value }, intArg)

        val result = BukkitCommands.tabComplete(mock(CommandSender::class.java), arrayOf("invalid", ""), command) { _, _ ->
            calls++
            listOf("unexpected")
        }

        assertThat(result).isEmpty()
        assertThat(calls).isZero()
    }

    @Test
    fun `greedy arguments continue their own completion after multiple tokens`() {
        val contexts = mutableListOf<List<String>>()
        val argument = strsArg.withContextualTabCompleter { context ->
            contexts.add(context.args)
            listOf("beta", "gold")
        }
        val command = Command.argument({ value: List<String> -> value }, argument)

        val result = BukkitCommands.tabComplete(mock(CommandSender::class.java), arrayOf("alpha", "be"), command) { _, _ ->
            error("Greedy argument completion must retain ownership of the current token")
        }

        assertThat(result).containsExactly("beta")
        assertThat(contexts).containsExactly(listOf("alpha", "be"))
    }

    @Test
    fun `a zero argument command can complete with a null result`() {
        val seen = mutableListOf<String?>()
        val command = Command.present<String?>(null)

        val result = BukkitCommands.tabComplete(mock(CommandSender::class.java), arrayOf(""), command) { _, value ->
            seen.add(value)
            listOf("custom")
        }

        assertThat(result).containsExactly("custom")
        assertThat(seen).containsExactly(null)
    }

    @Test
    fun `nested fallback help includes the successful prefix exactly once`() {
        val command = Command.mapping(pair("branch", Command.mapping(pair("leaf", Command.present(0)))
            .withFallback(Command.argument({ value: Int -> value }, intArg))))

        val result = BukkitCommands.getCommandUsages(mock(CommandSender::class.java), "cmd", arrayOf("root", "bad"),
            1, command, BukkitCommandConfig.empty).map { ChatColor.stripColor(it) }

        assertThat(result).containsExactly("/cmd root branch leaf", "/cmd root branch (int)")
    }

    @Test
    fun `help hiding policy applies before custom formatting and preserves the permission spec`() {
        val seenPermissions = mutableListOf<String>()
        val command = Command.present(0).withPermission("admin.lookup")
        val sender = mock(CommandSender::class.java)
        val config = BukkitCommandConfig.empty.withFormatter { help ->
            seenPermissions.add(help.spec.permission)
            "custom-help"
        }

        assertThat(BukkitCommands.getCommandUsages(sender, "cmd", emptyArray(), 0, command, config)).isEmpty()
        assertThat(seenPermissions).isEmpty()
        assertThat(BukkitCommands.getCommandUsages(sender, "cmd", emptyArray(), 0, command,
            config.withHideNoPermissionCommands(false))).containsExactly("custom-help")
        assertThat(seenPermissions).containsExactly("admin.lookup")
    }

    @Test
    fun `normalize nested YAML maps collections and configuration sections without mutating input`() {
        val nested = linkedMapOf<String, Any?>("unset" to null, "value" to 42)
        val section = mock(ConfigurationSection::class.java)
        `when`(section.getValues(false)).thenReturn(nested)
        val source = linkedMapOf<String, Any?>("section" to section, "items" to listOf(null, nested))

        val result = BukkitConverters.normalizeYamlMap(source)

        assertThat(result.keys).containsExactly("section", "items")
        assertThat(result["section"]).isEqualTo(nested)
        assertThat(result["items"]).isEqualTo(listOf(null, nested))
        assertThat(source["section"]).isSameAs(section)
        assertThat(nested.keys).containsExactly("unset", "value")
        assertThat(nested["unset"]).isNull()
    }

    @Test
    fun `converted duplicate map keys use the last value including null`() {
        val source = linkedMapOf<Any, Any?>(1 to "old", "1" to null, "next" to 42)
        val result = Converters.toMapAs({ entry -> pair(entry.a.toString(), entry.b) }, source).get()

        assertThat(result.keys).containsExactly("1", "next")
        assertThat(result["1"]).isNull()
        assertThat(result["next"]).isEqualTo(42)
    }

    @Test
    fun `registerPrime wires execution and custom completion without running callbacks at registration`() {
        val plugin = mock(JavaPlugin::class.java)
        val pluginCommand = mock(PluginCommand::class.java)
        `when`(plugin.getCommand("cmd")).thenReturn(pluginCommand)
        val executions = mutableListOf<Int>()
        val completions = mutableListOf<Int>()
        val command = Command.argument({ value: Int -> value }, intArg)

        BukkitCommands.registerPrime("cmd", command, { _, value -> executions.add(value) }, { _, value ->
            completions.add(value)
            listOf("value-$value")
        }, BukkitCommandConfig.empty, plugin)
        val executor = ArgumentCaptor.forClass(CommandExecutor::class.java)
        val completer = ArgumentCaptor.forClass(TabCompleter::class.java)
        verify(pluginCommand).setExecutor(executor.capture())
        verify(pluginCommand).setTabCompleter(completer.capture())
        assertThat(executions).isEmpty()
        assertThat(completions).isEmpty()

        val sender = mock(CommandSender::class.java)
        assertThat(executor.value.onCommand(sender, pluginCommand, "alias", arrayOf("42"))).isTrue()
        assertThat(completer.value.onTabComplete(sender, pluginCommand, "alias", arrayOf("42", "")))
            .containsExactly("value-42")
        assertThat(executions).containsExactly(42)
        assertThat(completions).containsExactly(42)
    }
}
