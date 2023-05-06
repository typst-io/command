package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.*;
import io.typecraft.command.algebra.Either;
import lombok.experimental.UtilityClass;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class BukkitCommands {
    /**
     * Register a command with simple usage
     *
     * @param <A>         The result of the command
     * @param commandName The main name of the command
     * @param command     The node of the command
     * @param executor    The executor of the command
     * @param plugin      The plugin that depends on the command
     */
    public static <A> void register(
            String commandName,
            Command<A> command,
            BiConsumer<CommandSender, A> executor,
            JavaPlugin plugin
    ) {
        registerPrime(commandName, command, executor, (sender, a) -> Collections.emptyList(), BukkitCommandConfig.empty, plugin);
    }

    /**
     * Register a command
     *
     * @param <A>          The result of the command
     * @param commandName  The main name of the command
     * @param command      The node of the command
     * @param executor     The executor of the command
     * @param tabCompleter The tab completer of the command
     * @param config       The configuration for the command
     * @param plugin       The plugin that depends on the command
     */
    public static <A> void registerPrime(
            String commandName, Command<A> command,
            BiConsumer<CommandSender, A> executor,
            BiFunction<CommandSender, A, List<String>> tabCompleter,
            BukkitCommandConfig config,
            JavaPlugin plugin
    ) {
        PluginTabExecutor<A> pluginTabExecutor = new PluginTabExecutor<>(config, command, commandName, plugin, executor, tabCompleter);
        PluginCommand pluginCmd = plugin.getCommand(commandName);
        if (pluginCmd == null) {
            throw new IllegalArgumentException(String.format("Unknown command name: '%s'", commandName));
        }
        pluginCmd.setExecutor(pluginTabExecutor);
        pluginCmd.setTabCompleter(pluginTabExecutor);
    }


    public static <A> Optional<CommandSuccess<A>> execute(CommandSender sender, String label, String[] args, Command<A> command, BukkitCommandConfig config) {
        Either<CommandFailure<A>, CommandSuccess<A>> result = Command.parse(args, command);
        if (result instanceof Either.Right) {
            CommandSuccess<A> success = ((Either.Right<CommandFailure<A>, CommandSuccess<A>>) result).getRight();
            return Optional.of(success);
        } else if (result instanceof Either.Left) {
            CommandFailure<A> failure = ((Either.Left<CommandFailure<A>, CommandSuccess<A>>) result).getLeft();
            for (String line : getFailureMessage(sender, label, failure, config)) {
                sender.sendMessage(line);
            }
        }
        return Optional.empty();
    }

    public static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return Command.tabComplete(args, command);
    }

    static <A> List<String> getCommandUsages(CommandSender sender, String label, String[] args, int position, Command<A> cmd, Function<BukkitCommandHelp, String> formatter) {
        String[] succArgs = args.length >= 1
                ? Arrays.copyOfRange(args, 0, position)
                : new String[0];
        return Command.getEntries(cmd).stream()
                .flatMap(pair -> {
                    List<String> usageArgs = pair.getKey().stream()
                            .flatMap(s -> Stream.concat(
                                    Arrays.stream(succArgs),
                                    Stream.of(s)
                            ))
                            .collect(Collectors.toList());
                    CommandSpec spec = Command.getSpec(pair.getValue());
                    String line = formatter.apply(BukkitCommandHelp.of(sender, label, usageArgs, spec));
                    return line.isEmpty() ? Stream.empty() : Stream.of(line);
                })
                .collect(Collectors.toList());
    }

    private static <A> List<String> getFailureMessage(CommandSender sender, String label, CommandFailure<A> failure, BukkitCommandConfig config) {
        if (failure instanceof CommandFailure.FewArguments) {
            CommandFailure.FewArguments<A> fewArgs = (CommandFailure.FewArguments<A>) failure;
            return getCommandUsages(sender, label, fewArgs.getArguments(), fewArgs.getIndex(), fewArgs.getCommand(), config.getFormatter());
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand<A> unknown = (CommandFailure.UnknownSubCommand<A>) failure;
            String input = unknown.getArguments()[unknown.getIndex()];
            List<String> usages = new ArrayList<>(getCommandUsages(
                    sender, label, unknown.getArguments(), unknown.getIndex(), unknown.getCommand(), config.getFormatter()
            ));
            usages.add(String.format("Command '%s' doesn't exists!", input));
            return usages;
        }
        return Collections.singletonList("Wrong command!");
    }

    private static class PluginTabExecutor<A> implements CommandExecutor, TabCompleter, PluginIdentifiableCommand {
        private final BukkitCommandConfig config;
        private final Command<A> command;
        private final String commandName;
        private final Plugin plugin;
        private final BiConsumer<CommandSender, A> executor;
        private final BiFunction<CommandSender, A, List<String>> tabCompleter;

        public PluginTabExecutor(BukkitCommandConfig config, Command<A> command, String commandName, Plugin plugin, BiConsumer<CommandSender, A> executor, BiFunction<CommandSender, A, List<String>> tabCompleter) {
            this.config = config;
            this.command = command;
            this.commandName = commandName;
            this.plugin = plugin;
            this.executor = executor;
            this.tabCompleter = tabCompleter;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
            execute(sender, label, args, command, config)
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
