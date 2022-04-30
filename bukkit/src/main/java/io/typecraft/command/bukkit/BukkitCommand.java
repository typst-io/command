package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.*;
import io.vavr.control.Either;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class BukkitCommand {
    public static <A> void register(
            String commandName,
            BiConsumer<CommandSender, A> executor,
            BiFunction<CommandSender, A, List<String>> tabCompleter,
            JavaPlugin plugin,
            Command<A> command
    ) {
        PluginTabExecutor<A> pluginTabExecutor = new PluginTabExecutor<>(command, plugin, executor, tabCompleter);
        PluginCommand pluginCmd = plugin.getCommand(commandName);
        if (pluginCmd == null) {
            throw new IllegalArgumentException(String.format("Unknown command name: '%s'", commandName));
        }
        pluginCmd.setExecutor(pluginTabExecutor);
        pluginCmd.setTabCompleter(pluginTabExecutor);
    }

    public static <A> Optional<CommandSuccess<A>> execute(CommandSender sender, String[] args, Command<A> command) {
        Either<CommandFailure,  CommandSuccess<A>> result = Command.parse(args, command);
        if (result.isRight()) {
            CommandSuccess<A> success = result.get();
            return Optional.of(success);
        } else if (result.isLeft()) {
            CommandFailure failure = result.getLeft();
            sender.sendMessage(getFailureMessage(failure));
        }
        return Optional.empty();
    }

    public static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return Command.tabComplete(args, command);
    }

    private static String getFailureMessage(CommandFailure failure) {
        if (failure instanceof CommandFailure.FewArguments) {
            return "명령어 추가인자를 입력해주세요!";
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand unknown = (CommandFailure.UnknownSubCommand) failure;
            String input = unknown.getArguments()[unknown.getIndex()];
            return String.format("'%s' 명령어는 존재하지 않습니다!", input);
        }
        return "잘못된 명령어입니다!";
    }

    private static class PluginTabExecutor<A> implements CommandExecutor, TabCompleter, PluginIdentifiableCommand {
        private final Command<A> command;
        private final Plugin plugin;
        private final BiConsumer<CommandSender, A> executor;
        private final BiFunction<CommandSender, A, List<String>> tabCompleter;

        public PluginTabExecutor(Command<A> command, Plugin plugin, BiConsumer<CommandSender, A> executor, BiFunction<CommandSender, A, List<String>> tabCompleter) {
            this.command = command;
            this.plugin = plugin;
            this.executor = executor;
            this.tabCompleter = tabCompleter;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
            execute(sender, args, command)
                    .ifPresent(succ -> executor.accept(sender, succ.getCommand()));
            return true;
        }

        @Nullable
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
            CommandTabResult<A> result = tabComplete(args, command);
            if (result instanceof CommandTabResult.Suggestion) {
                return ((CommandTabResult.Suggestion<A>) result).getSuggestions();
            } else if (result instanceof CommandTabResult.Present) {
                A value = ((CommandTabResult.Present<A>) result).getCommand();
                return tabCompleter.apply(sender, value);
            }
            return Collections.emptyList();
        }

        @NotNull
        @Override
        public Plugin getPlugin() {
            return plugin;
        }
    }
}
