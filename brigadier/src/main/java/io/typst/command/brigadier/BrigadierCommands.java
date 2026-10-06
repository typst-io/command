package io.typst.command.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.typst.command.Argument;
import io.typst.command.Command;
import io.typst.command.CommandFailure;
import io.typst.command.CommandSource;
import io.typst.command.ParseContext;
import io.typst.command.algebra.Either;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
        buildNode(root, command, executor, Collections.emptyList());
        return root;
    }

    private static <S, A> void buildNode(
            ArgumentBuilder<S, ?> parent,
            Command<A> command,
            BiConsumer<S, A> executor,
            List<String> commandPath) {
        if (command instanceof Command.Mapping) {
            Command.Mapping<A> mapping = (Command.Mapping<A>) command;
            Map<String, Command<A>> commandMap = mapping.getCommandMap();

            for (Map.Entry<String, Command<A>> entry : commandMap.entrySet()) {
                String key = entry.getKey();
                Command<A> subCommand = entry.getValue();

                LiteralArgumentBuilder<S> literalNode = literal(key);
                List<String> subPath = new ArrayList<>(commandPath);
                subPath.add(key);
                buildNode(literalNode, subCommand, executor, Collections.unmodifiableList(subPath));
                parent.then(literalNode);
            }

            // Handle fallback if exists
            mapping.getFallback().ifPresent(fallback -> buildNode(parent, fallback, executor, commandPath));

        } else if (command instanceof Command.Parser) {
            Command.Parser<A> parser = (Command.Parser<A>) command;
            List<Argument<?>> arguments = parser.getArguments();

            buildArgumentChain(parent, parser, arguments, 0, executor, commandPath);
        }
    }

    private static <S, A> void buildArgumentChain(
            ArgumentBuilder<S, ?> parent,
            Command.Parser<A> parser,
            List<Argument<?>> arguments,
            int index,
            BiConsumer<S, A> executor,
            List<String> commandPath) {
        if (arguments.subList(index, arguments.size()).stream().allMatch(Argument::isOptional)) {
            parent.executes(ctx -> {
                A result = parseFromContext(ctx, parser, arguments, index);
                executor.accept(ctx.getSource(), result);
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            });
        }
        if (index >= arguments.size()) {
            return;
        }

        Argument<?> arg = arguments.get(index);
        if (arg.isGreedy() && index != arguments.size() - 1) {
            throw new IllegalArgumentException("Greedy argument '" + arg.getName() + "' must be last");
        }
        ArgumentType<?> brigadierType = toBrigadierType(arg);
        String argName = arg.getName() + index; // Ensure unique names

        RequiredArgumentBuilder<S, ?> argNode = argument(argName, brigadierType);
        argNode.suggests(createSuggestionProvider(arguments, index, commandPath));
        buildArgumentChain(argNode, parser, arguments, index + 1, executor, commandPath);
        parent.then(argNode);
    }

    private static <S, A> A parseFromContext(
            CommandContext<S> ctx,
            Command.Parser<A> parser,
            List<Argument<?>> arguments,
            int argumentCount) throws CommandSyntaxException {
        return parseAndGetFromArgs(readArguments(ctx, arguments, argumentCount).toArray(new String[0]), parser);
    }

    private static List<String> readArguments(CommandContext<?> ctx, List<Argument<?>> arguments, int argumentCount) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < argumentCount; i++) {
            Argument<?> argument = arguments.get(i);
            String value = String.valueOf(ctx.getArgument(argument.getName() + i, Object.class));
            if (argument.isGreedy()) {
                tokens.addAll(splitGreedy(value));
            } else {
                tokens.add(value);
            }
        }
        return tokens;
    }

    private static List<String> splitGreedy(String input) {
        return Arrays.stream(input.split("\\p{javaWhitespace}+"))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());
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
        if (argument.isGreedy()) {
            return StringArgumentType.greedyString();
        }
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
        } else {
            // Default to string for String.class and unknown types
            return StringArgumentType.string();
        }
    }

    private static <S> SuggestionProvider<S> createSuggestionProvider(
            List<Argument<?>> arguments, int index, List<String> commandPath) {
        Argument<?> argument = arguments.get(index);
        return (ctx, builder) -> {
            List<String> tokens = new ArrayList<>(commandPath);
            tokens.addAll(readArguments(ctx, arguments, index));
            SuggestionsBuilder completionBuilder = builder;
            if (argument.isGreedy()) {
                String input = builder.getInput();
                int tokenStart = input.length();
                while (tokenStart > builder.getStart() && !Character.isWhitespace(input.charAt(tokenStart - 1))) {
                    tokenStart--;
                }
                tokens.addAll(splitGreedy(input.substring(builder.getStart(), tokenStart)));
                completionBuilder = builder.createOffset(tokenStart);
            }
            tokens.add(completionBuilder.getRemaining());
            ParseContext parseContext = new ParseContext(
                    new CommandSource(""),
                    Collections.unmodifiableList(tokens)
            );

            List<String> completions = argument.getContextualTabCompleter().apply(parseContext);
            String remaining = completionBuilder.getRemaining();
            String prefix = (argument.isGreedy() ? remaining : readCompletionPrefix(remaining))
                    .toLowerCase(Locale.ROOT);
            for (String completion : completions) {
                if (completion.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completionBuilder.suggest(argument.isGreedy()
                            ? completion
                            : StringArgumentType.escapeIfRequired(completion));
                }
            }

            return completionBuilder.buildFuture();
        };
    }

    private static String readCompletionPrefix(String input) throws CommandSyntaxException {
        if (input.isEmpty() || !StringReader.isQuotedStringStart(input.charAt(0))) {
            return input;
        }
        try {
            return new StringReader(input).readString();
        } catch (CommandSyntaxException exception) {
            if (exception.getType() != CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote()) {
                throw exception;
            }
            // Complete the open quote for the native reader. A trailing escape is still being typed.
            int escapeStart = input.length();
            while (escapeStart > 0 && input.charAt(escapeStart - 1) == '\\') {
                escapeStart--;
            }
            if ((input.length() - escapeStart) % 2 != 0) {
                input = input.substring(0, input.length() - 1);
            }
            return new StringReader(input + input.charAt(0)).readString();
        }
    }
}
