package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.*;
import io.typecraft.command.bukkit.config.BukkitCommandConfig;
import io.typecraft.command.config.CommandConfig;
import io.typecraft.command.i18n.Language;
import io.typecraft.command.i18n.MessageId;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class BukkitCommand {
    /**
     * Register a command with CommandConfig
     *
     * @param commandName  The main name of the command
     * @param executor     The executor of the command
     * @param tabCompleter The tab completer of the command
     * @param getConfig    The config for internalization
     * @param plugin       The plugin that depends on the command
     * @param command      The node of the command
     * @param <A>          The result of the command
     */
    public static <A> void registerWithConfig(
            String commandName,
            BiConsumer<CommandSender, A> executor,
            BiFunction<CommandSender, A, List<String>> tabCompleter,
            Supplier<CommandConfig> getConfig,
            JavaPlugin plugin,
            Command<A> command
    ) {
        PluginTabExecutor<A> pluginTabExecutor = new PluginTabExecutor<>(command, commandName, plugin, getConfig, executor, tabCompleter);
        PluginCommand pluginCmd = plugin.getCommand(commandName);
        if (pluginCmd == null) {
            throw new IllegalArgumentException(String.format("Unknown command name: '%s'", commandName));
        }
        pluginCmd.setExecutor(pluginTabExecutor);
        pluginCmd.setTabCompleter(pluginTabExecutor);
    }

    /**
     * To use this register, your plugin must depend on `BukkitCommand` plugin. If you don't, use `BukkitCommand.registerWithConfig` instead.
     *
     * @param commandName  The main name of the command
     * @param executor     The executor of the command
     * @param tabCompleter The tab completer of the command
     * @param plugin       The plugin that depends on the command
     * @param command      The node of the command
     * @param <A>          The result of the command
     */
    public static <A> void register(
            String commandName,
            BiConsumer<CommandSender, A> executor,
            BiFunction<CommandSender, A, List<String>> tabCompleter,
            JavaPlugin plugin,
            Command<A> command
    ) {
        registerWithConfig(commandName, executor, tabCompleter, () -> {
            Plugin kernel = Bukkit.getPluginManager().getPlugin("BukkitCommand");
            return kernel instanceof CommandBukkitPlugin
                    ? ((CommandBukkitPlugin) kernel).getCommandConfig()
                    : BukkitCommandConfig.ofDefault();
        }, plugin, command);
    }


    public static <A> Optional<CommandSuccess<A>> execute(Map<String, String> langs, CommandSender sender, String label, String[] args, Command<A> command) {
        Either<CommandFailure<A>, CommandSuccess<A>> result = Command.parse(args, command);
        if (result.isRight()) {
            CommandSuccess<A> success = result.get();
            return Optional.of(success);
        } else if (result.isLeft()) {
            CommandFailure<A> failure = result.getLeft();
            for (String line : getFailureMessage(langs, label, failure)) {
                sender.sendMessage(line);
            }
        }
        return Optional.empty();
    }

    public static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return Command.tabComplete(args, command);
    }

    private static <A> List<String> getCommandUsages(Map<String, String> langs, String label, String[] args, int position, Command<A> cmd) {
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
                            : " §e" + spec.getArguments().stream().map(s -> String.format("(%s)", s.getMessage(langs))).collect(Collectors.joining(" "));
                    String description = spec.getDescriptionId().getMessage(langs);
                    String descSuffix = description.isEmpty()
                            ? ""
                            : " §f- " + description;
                    return String.format("§a/%s %s", label, String.join(" ", usageArgs)) + argSuffix + descSuffix;
                })
                .collect(Collectors.toList());
    }

    private static <A> List<String> getFailureMessage(Map<String, String> langs, String label, CommandFailure<A> failure) {
        if (failure instanceof CommandFailure.FewArguments) {
            CommandFailure.FewArguments<A> fewArgs = (CommandFailure.FewArguments<A>) failure;
            return getCommandUsages(langs, label, fewArgs.getArguments(), fewArgs.getIndex(), fewArgs.getCommand());
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand<A> unknown = (CommandFailure.UnknownSubCommand<A>) failure;
            String input = unknown.getArguments()[unknown.getIndex()];
            List<String> usages = new ArrayList<>(getCommandUsages(
                    langs, label, unknown.getArguments(), unknown.getIndex(), unknown.getCommand()
            ));
            usages.add(String.format(MessageId.commandNotExists.getMessage(langs), input));
            return usages;
        }
        return Collections.singletonList(MessageId.commandWrongUsage.getMessage(langs));
    }

    private static class PluginTabExecutor<A> implements CommandExecutor, TabCompleter, PluginIdentifiableCommand {
        private final Command<A> command;
        private final String commandName;
        private final Plugin plugin;
        private final Supplier<CommandConfig> getConfig;
        private final BiConsumer<CommandSender, A> executor;
        private final BiFunction<CommandSender, A, List<String>> tabCompleter;

        public PluginTabExecutor(Command<A> command, String commandName, Plugin plugin, Supplier<CommandConfig> getConfig, BiConsumer<CommandSender, A> executor, BiFunction<CommandSender, A, List<String>> tabCompleter) {
            this.command = command;
            this.commandName = commandName;
            this.plugin = plugin;
            this.getConfig = getConfig;
            this.executor = executor;
            this.tabCompleter = tabCompleter;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
            CommandConfig config = getConfig.get();
            Player player = sender instanceof Player ? ((Player) sender) : null;
            Locale locale = player != null ? Language.parseLocaleFrom(player.getLocale()).orElse(null) : config.getDefaultLocale();
            Map<String, String> langs = config.getPluginMessagesWithBase(locale, plugin.getName());
            execute(langs, sender, label, args, command)
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
