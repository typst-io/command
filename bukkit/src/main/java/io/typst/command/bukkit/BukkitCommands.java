package io.typst.command.bukkit;

import io.typst.command.*;
import io.typst.command.Command;
import io.typst.command.algebra.Either;
import io.typst.command.algebra.Tuple2;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
     * Register a command with simple usage and custom config
     *
     * @param <A>         The result of the command
     * @param commandName The main name of the command
     * @param command     The node of the command
     * @param executor    The executor of the command
     * @param config      The configuration for the command
     * @param plugin      The plugin that depends on the command
     */
    public static <A> void register(
            String commandName,
            Command<A> command,
            BiConsumer<CommandSender, A> executor,
            BukkitCommandConfig config,
            JavaPlugin plugin
    ) {
        registerPrime(commandName, command, executor, (sender, a) -> Collections.emptyList(), config, plugin);
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
        resolveCommand(args, command, false)
                .ifPresent(route -> BukkitControlFlows.validatePermission(route.getA(), sender));
        Either<CommandFailure<A>, CommandSuccess<A>> result = Command.parse(args, command);
        if (result instanceof Either.Right) {
            CommandSuccess<A> success = ((Either.Right<CommandFailure<A>, CommandSuccess<A>>) result).getRight();
            BukkitControlFlows.validatePermission(success.getNode(), sender);
            return Optional.of(success);
        } else if (result instanceof Either.Left) {
            CommandFailure<A> failure = ((Either.Left<CommandFailure<A>, CommandSuccess<A>>) result).getLeft();
            sender.sendMessage(" ");
            sendFailureMessage(sender, label, failure, config);
        }
        return Optional.empty();
    }

    public static <A> CommandTabResult<A> tabComplete(CommandSource source, String[] args, Command<A> command) {
        return Command.tabComplete(source, args, command);
    }

    public static <A> List<String> tabComplete(CommandSender sender, String[] args, Command<A> cmd, BiFunction<CommandSender, A, List<String>> tabCompleter) {
        CommandSource source = sender instanceof Player
                ? new CommandSource(((Player) sender).getUniqueId().toString())
                : new CommandSource("");
        Optional<Tuple2<Command<A>, Integer>> route = resolveCommand(args, cmd, true);
        if (!route.isPresent() || !hasPermission(sender, route.get().getA())) {
            return Collections.emptyList();
        }
        CommandTabResult<A> result = Command.tabCompleteWithIndex(route.get().getB(), source, args, route.get().getA());
        if (result instanceof CommandTabResult.Suggestions) {
            CommandTabResult.Suggestions<A> suggestions = (CommandTabResult.Suggestions<A>) result;
            return suggestions.getSuggestions().stream()
                    .flatMap(pair -> {
                        String suggestion = pair.getA();
                        Command<A> command = pair.getB().orElse(null);
                        CommandSpec spec = command != null ? CommandSpec.from(command) : CommandSpec.empty;
                        String perm = spec.getPermission();
                        return perm.isEmpty() || sender.hasPermission(perm)
                                ? Stream.of(suggestion)
                                : Stream.empty();
                    })
                    .collect(Collectors.toList());
        } else if (result instanceof CommandTabResult.Present) {
            if (!hasPermission(sender, route.get().getA())) {
                return Collections.emptyList();
            }
            A value = ((CommandTabResult.Present<A>) result).getCommand();
            return tabCompleter.apply(sender, value);
        }
        return Collections.emptyList();
    }

    private static <A> Optional<Tuple2<Command<A>, Integer>> resolveCommand(
            String[] args, Command<A> command, boolean completing) {
        int index = 0;
        // During completion the final token belongs to the current mapping or argument completer.
        while (command instanceof Command.Mapping && (!completing || index < args.length - 1)) {
            Command.Mapping<A> mapping = (Command.Mapping<A>) command;
            Command<A> next = index < args.length ? mapping.getCommandMap().get(args[index]) : null;
            if (next != null) {
                index++;
            } else {
                next = mapping.getFallback().orElse(null);
            }
            if (next == null) {
                return Optional.empty();
            }
            command = next;
        }
        return Optional.of(new Tuple2<>(command, index));
    }

    private static boolean hasPermission(CommandSender sender, Command<?> command) {
        String permission = CommandSpec.from(command).getPermission();
        return permission.isEmpty() || sender.hasPermission(permission);
    }

    /**
     * @param args     the input args
     * @param position the last position that parsed successfully so that can be ignored
     * @return usages
     */
    static <A> List<String> getCommandUsages(CommandSender sender, String label, String[] args, int position, Command<A> cmd, BukkitCommandConfig config) {
        return getCommandHelpEntries(sender, label, args, position, cmd, config).stream()
                .map(config.getFormatter())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }

    private static <A> List<BukkitCommandHelp> getCommandHelpEntries(CommandSender sender, String label, String[] args, int position, Command<A> cmd, BukkitCommandConfig config) {
        Player player = sender instanceof Player ? ((Player) sender) : null;
        String locale = player != null ? player.getLocale() : Locale.getDefault().toString().toLowerCase();
        String[] succArgs = args.length >= 1
                ? Arrays.copyOfRange(args, 0, position)
                : new String[0];
        return Command.getEntries(cmd).stream()
                .flatMap(pair -> {
                    List<String> theArgs = pair.getKey();
                    CommandSpec spec = CommandSpec.from(pair.getValue());
                    String perm = spec.getPermission();
                    // skip if the config option is true, and the player has no permission
                    if (config.isHideNoPermissionCommands() && !perm.isEmpty() && !sender.hasPermission(perm)) {
                        return Stream.empty();
                    }
                    List<String> usageArgs = Stream.concat(Arrays.stream(succArgs), theArgs.stream())
                            .collect(Collectors.toList());
                    return Stream.of(BukkitCommandHelp.of(sender, label, usageArgs, spec, locale));
                })
                .collect(Collectors.toList());
    }

    private static <A> void sendCommandUsages(CommandSender sender, String label, String[] args, int position, Command<A> cmd, BukkitCommandConfig config) {
        for (BukkitCommandHelp help : getCommandHelpEntries(sender, label, args, position, cmd, config)) {
            String line = config.getFormatter().apply(help);
            if (line.isEmpty()) {
                continue;
            }
            if (sender instanceof Player) {
                String suggestion = "/" + help.getLabel()
                        + (help.getArguments().isEmpty() ? "" : " " + String.join(" ", help.getArguments()))
                        + (help.getSpec().getArguments().isEmpty() ? "" : " ");
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(line));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
                ((Player) sender).spigot().sendMessage(component);
            } else {
                sender.sendMessage(line);
            }
        }
    }

    private static <A> void sendFailureMessage(CommandSender sender, String label, CommandFailure<A> failure, BukkitCommandConfig config) {
        LangKey langKey = BukkitControlFlows.getLocale(sender);
        if (failure instanceof CommandFailure.FewArguments) {
            CommandFailure.FewArguments<A> fewArgs = (CommandFailure.FewArguments<A>) failure;
            sendCommandUsages(sender, label, fewArgs.getArguments(), fewArgs.getIndex(), fewArgs.getCommand(), config);
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand<A> unknown = (CommandFailure.UnknownSubCommand<A>) failure;
            String input = unknown.getArguments()[unknown.getIndex()];
            sendCommandUsages(
                    sender, label, unknown.getArguments(), unknown.getIndex(), unknown.getCommand(), config
            );
            String unknownMsg = config.formatMessage(langKey, MessageKey.UNKNOWN_SUB_COMMAND, input);
            sender.sendMessage(unknownMsg);
        } else if (failure instanceof CommandFailure.ParsingFailure) {
            CommandFailure.ParsingFailure<A> parsingFailure = (CommandFailure.ParsingFailure<A>) failure;
            sendCommandUsages(sender, label, parsingFailure.getArguments(), parsingFailure.getIndex(), parsingFailure.getCommand(), config);
            String message = config.formatMessage(langKey, MessageKey.INVALID_COMMAND);
            sender.sendMessage(message);
        } else {
            String message = config.formatMessage(langKey, MessageKey.INVALID_COMMAND);
            sender.sendMessage(message);
        }
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
            try {
                execute(sender, label, args, command, config)
                        .ifPresent(succ -> executor.accept(sender, succ.getCommand()));
            } catch (CommandCancellationException ex) {
                MessageKey messageKey = ex.getMessageKey();
                if (messageKey != null) {
                    LangKey langKey = BukkitControlFlows.getLocale(sender);
                    sender.sendMessage(config.formatMessage(langKey, messageKey, ex.getMessageArgs()));
                } else {
                    sender.sendMessage(ex.getMessage());
                }
            }
            return true;
        }

        @Nullable
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
            return tabComplete(sender, args, command, tabCompleter);
        }

        @NotNull
        @Override
        public Plugin getPlugin() {
            return plugin;
        }
    }
}
