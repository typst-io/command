package io.typst.command.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.typst.command.Argument;
import io.typst.command.Command;
import io.typst.command.CommandFailure;
import io.typst.command.CommandSource;
import io.typst.command.ParseContext;
import io.typst.command.algebra.Either;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * Utility class for converting {@link io.typst.command.Command} trees into Brigadier command trees.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Command<MyCommand> command = Command.mapping(
 *     pair("add", Command.argument(AddItem::new, intArg, strArg)),
 *     pair("remove", Command.argument(RemoveItem::new, intArg))
 * );
 *
 * LiteralArgumentBuilder<CommandSourceStack> brigadierCmd =
 *     BrigadierCommands.from("item", command, (source, result) -> {
 *         if (result instanceof AddItem) {
 *             // handle add
 *         }
 *     });
 *
 * dispatcher.register(brigadierCmd);
 * }</pre>
 */
public class BrigadierCommands {

    /**
     * Converts a {@link Command} tree into a Brigadier {@link LiteralArgumentBuilder}.
     *
     * @param <S>      the source type (e.g., CommandSourceStack)
     * @param <A>      the result type of the command
     * @param name     the root command name (e.g., "item")
     * @param command  the command tree to convert
     * @param executor the executor that handles the parsed command result
     * @return a Brigadier command builder ready to be registered
     */
    public static <S, A> LiteralArgumentBuilder<S> from(
            String name,
            Command<A> command,
            BiConsumer<S, A> executor) {
        LiteralArgumentBuilder<S> root = literal(name);
        buildNode(root, command, executor);
        return root;
    }

    private static <S, A> void buildNode(
            ArgumentBuilder<S, ?> parent,
            Command<A> command,
            BiConsumer<S, A> executor) {
        if (command instanceof Command.Mapping) {
            Command.Mapping<A> mapping = (Command.Mapping<A>) command;
            Map<String, Command<A>> commandMap = mapping.getCommandMap();

            for (Map.Entry<String, Command<A>> entry : commandMap.entrySet()) {
                String key = entry.getKey();
                Command<A> subCommand = entry.getValue();

                LiteralArgumentBuilder<S> literalNode = literal(key);
                buildNode(literalNode, subCommand, executor);
                parent.then(literalNode);
            }

            // Handle fallback if exists
            mapping.getFallback().ifPresent(fallback -> buildNode(parent, fallback, executor));

        } else if (command instanceof Command.Parser) {
            Command.Parser<A> parser = (Command.Parser<A>) command;
            List<Argument<?>> arguments = parser.getArguments();

            if (arguments.isEmpty()) {
                // No arguments - just execute
                parent.executes(ctx -> {
                    A result = parseAndGet(ctx, parser);
                    executor.accept(ctx.getSource(), result);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                });
            } else {
                // Build argument chain
                buildArgumentChain(parent, parser, arguments, 0, executor);
            }
        }
    }

    private static <S, A> void buildArgumentChain(
            ArgumentBuilder<S, ?> parent,
            Command.Parser<A> parser,
            List<Argument<?>> arguments,
            int index,
            BiConsumer<S, A> executor) {
        if (index >= arguments.size()) {
            // All arguments processed - add executor
            parent.executes(ctx -> {
                A result = parseFromContext(ctx, parser, arguments);
                executor.accept(ctx.getSource(), result);
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            });
            return;
        }

        Argument<?> arg = arguments.get(index);
        ArgumentType<?> brigadierType = toBrigadierType(arg);
        String argName = arg.getName() + index; // Ensure unique names

        RequiredArgumentBuilder<S, ?> argNode = argument(argName, brigadierType);
        argNode.suggests(createSuggestionProvider(arg));
        buildArgumentChain(argNode, parser, arguments, index + 1, executor);
        parent.then(argNode);
    }

    private static <S, A> A parseFromContext(
            CommandContext<S> ctx,
            Command.Parser<A> parser,
            List<Argument<?>> arguments) throws CommandSyntaxException {
        // Extract arguments from Brigadier context and build args array
        String[] args = new String[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            Argument<?> arg = arguments.get(i);
            String argName = arg.getName() + i;
            try {
                Object value = ctx.getArgument(argName, Object.class);
                args[i] = String.valueOf(value);
            } catch (IllegalArgumentException e) {
                args[i] = "";
            }
        }
        return parseAndGetFromArgs(args, parser);
    }

    private static <A> A parseAndGet(CommandContext<?> ctx, Command.Parser<A> parser) throws CommandSyntaxException {
        // For commands with no arguments
        String[] emptyArgs = new String[0];
        Either<CommandFailure<A>, io.typst.command.CommandSuccess<A>> result =
                Command.parse(emptyArgs, parser);

        if (result instanceof Either.Right) {
            return ((Either.Right<CommandFailure<A>, io.typst.command.CommandSuccess<A>>) result)
                    .getRight()
                    .getCommand();
        } else {
            CommandFailure<A> failure = ((Either.Left<CommandFailure<A>, io.typst.command.CommandSuccess<A>>) result)
                    .getLeft();
            throw createException(failure);
        }
    }

    private static <A> A parseAndGetFromArgs(String[] args, Command.Parser<A> parser) throws CommandSyntaxException {
        Either<CommandFailure<A>, io.typst.command.CommandSuccess<A>> result =
                Command.parse(args, parser);

        if (result instanceof Either.Right) {
            return ((Either.Right<CommandFailure<A>, io.typst.command.CommandSuccess<A>>) result)
                    .getRight()
                    .getCommand();
        } else {
            CommandFailure<A> failure = ((Either.Left<CommandFailure<A>, io.typst.command.CommandSuccess<A>>) result)
                    .getLeft();
            throw createException(failure);
        }
    }

    private static <A> CommandSyntaxException createException(CommandFailure<A> failure) {
        String message;
        int index;

        if (failure instanceof CommandFailure.ParsingFailure) {
            CommandFailure.ParsingFailure<A> pf = (CommandFailure.ParsingFailure<A>) failure;
            index = pf.getIndex();
            List<Argument<?>> failedArgs = pf.getArgs();
            if (!failedArgs.isEmpty()) {
                Argument<?> firstArg = failedArgs.get(0);
                message = "Invalid argument at position " + index + ": expected " + firstArg.getName();
            } else {
                message = "Invalid argument at position " + index;
            }
        } else if (failure instanceof CommandFailure.FewArguments) {
            CommandFailure.FewArguments<A> fa = (CommandFailure.FewArguments<A>) failure;
            index = fa.getIndex();
            message = "Not enough arguments";
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand<A> us = (CommandFailure.UnknownSubCommand<A>) failure;
            index = us.getIndex();
            String[] args = us.getArguments();
            String unknownArg = index < args.length ? args[index] : "";
            message = "Unknown subcommand: " + unknownArg;
        } else {
            index = 0;
            message = "Command parsing failed";
        }

        return new SimpleCommandExceptionType(new LiteralMessage(message)).create();
    }

    private static ArgumentType<?> toBrigadierType(Argument<?> argument) {
        Class<?> type = argument.getClassType();

        if (type == Integer.class || type == int.class) {
            return IntegerArgumentType.integer();
        } else if (type == Long.class || type == long.class) {
            return LongArgumentType.longArg();
        } else if (type == Float.class || type == float.class) {
            return FloatArgumentType.floatArg();
        } else if (type == Double.class || type == double.class) {
            return DoubleArgumentType.doubleArg();
        } else if (type == Boolean.class || type == boolean.class) {
            return BoolArgumentType.bool();
        } else if (type == List.class) {
            // For greedy string arguments (strsArg)
            return StringArgumentType.greedyString();
        } else {
            // Default to string for String.class and unknown types
            return StringArgumentType.string();
        }
    }

    private static <S> SuggestionProvider<S> createSuggestionProvider(Argument<?> argument) {
        return (ctx, builder) -> {
            // Create ParseContext for the tab completer
            // Note: CommandSource is created with empty ID since Brigadier's source type <S>
            // may not be directly convertible to CommandSource
            ParseContext parseContext = new ParseContext(
                    new CommandSource(""),
                    Collections.emptyList()
            );

            // Get completions from the Argument's contextual tab completer
            List<String> completions = argument.getContextualTabCompleter().apply(parseContext);

            // Add all completions to the suggestions builder
            for (String completion : completions) {
                builder.suggest(completion);
            }

            return builder.buildFuture();
        };
    }
}