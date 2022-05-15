package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.*;
import io.vavr.control.Either;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitCommand {
    public static <A> void register(
            String commandName,
            BiConsumer<CommandSender, A> executor,
            BiFunction<CommandSender, A, List<String>> tabCompleter,
            JavaPlugin plugin,
            Command<A> command
    ) {
        PluginTabExecutor<A> pluginTabExecutor = new PluginTabExecutor<>(command, commandName, plugin, executor, tabCompleter);
        PluginCommand pluginCmd = plugin.getCommand(commandName);
        if (pluginCmd == null) {
            throw new IllegalArgumentException(String.format("Unknown command name: '%s'", commandName));
        }
        pluginCmd.setExecutor(pluginTabExecutor);
        pluginCmd.setTabCompleter(pluginTabExecutor);
    }

    public static <A> Optional<CommandSuccess<A>> execute(CommandSender sender, String label, String[] args, Command<A> command) {
        Either<CommandFailure<A>, CommandSuccess<A>> result = Command.parse(args, command);
        if (result.isRight()) {
            CommandSuccess<A> success = result.get();
            return Optional.of(success);
        } else if (result.isLeft()) {
            CommandFailure<A> failure = result.getLeft();
            for (String line : getFailureMessage(label, failure)) {
                sender.sendMessage(line);
            }
        }
        return Optional.empty();
    }

    public static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return Command.tabComplete(args, command);
    }

    private static <A> List<String> getCommandUsages(String label, String[] args, int position, Command<A> cmd) {
        String[] succArgs = args.length >= 1
                ? Arrays.copyOfRange(args, 0, position)
                : new String[0];
        return Command.getEntries(cmd).stream()
                .map(pair -> {
                    List<String> usageArgs = pair.getKey().stream()
                            .flatMap(s -> Stream.concat(
                                    Arrays.stream(succArgs),
                                    Stream.of(s)
                            ))
                            .collect(Collectors.toList());
                    CommandSpec spec = Command.getSpec(pair.getValue());
                    String argSuffix = spec.getArguments().isEmpty()
                            ? ""
                            : " §e" + spec.getArguments().stream().map(s -> String.format("(%s)", s)).collect(Collectors.joining(" "));
                    String descSuffix = spec.getDescription().isEmpty()
                            ? ""
                            : " §f- " + spec.getDescription();
                    return String.format("§a/%s %s", label, String.join(" ", usageArgs)) + argSuffix + descSuffix;
                })
                .collect(Collectors.toList());
    }

    private static <A> List<String> getFailureMessage(String label, CommandFailure<A> failure) {
        if (failure instanceof CommandFailure.FewArguments) {
            CommandFailure.FewArguments<A> fewArgs = (CommandFailure.FewArguments<A>) failure;
            return getCommandUsages(label, fewArgs.getArguments(), fewArgs.getIndex(), fewArgs.getCommand());
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand<A> unknown = (CommandFailure.UnknownSubCommand<A>) failure;
            String input = unknown.getArguments()[unknown.getIndex()];
            List<String> usages = new ArrayList<>(getCommandUsages(
                    label, unknown.getArguments(), unknown.getIndex(), unknown.getCommand()
            ));
            usages.add(String.format("'%s' 명령어는 존재하지 않습니다!", input));
            return usages;
        }
        return Collections.singletonList("잘못된 명령어입니다!");
    }

    private static class PluginTabExecutor<A> implements CommandExecutor, TabCompleter, PluginIdentifiableCommand {
        private final Command<A> command;
        private final String commandName;
        private final Plugin plugin;
        private final BiConsumer<CommandSender, A> executor;
        private final BiFunction<CommandSender, A, List<String>> tabCompleter;

        public PluginTabExecutor(Command<A> command, String commandName, Plugin plugin, BiConsumer<CommandSender, A> executor, BiFunction<CommandSender, A, List<String>> tabCompleter) {
            this.command = command;
            this.commandName = commandName;
            this.plugin = plugin;
            this.executor = executor;
            this.tabCompleter = tabCompleter;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
            execute(sender, label, args, command)
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
