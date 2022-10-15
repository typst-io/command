package io.typecraft.command;

import io.typecraft.command.i18n.MessageId;
import io.vavr.*;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.Data;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Command<A> {
    <B> Command<B> map(Function<? super A, ? extends B> f);

    @Data
    @With
    class Mapping<A> implements Command<A> {
        private final Map<String, Command<A>> map;
        private final Command<A> fallback;

        private Mapping(Map<String, Command<A>> map, @Nullable Command<A> fallback) {
            this.map = map;
            this.fallback = fallback;
        }

        @Override
        public <B> Mapping<B> map(Function<? super A, ? extends B> f) {
            Map<String, Command<B>> newMap = new HashMap<>(this.map.size());
            for (Map.Entry<String, Command<A>> pair : map.entrySet()) {
                newMap.put(pair.getKey(), pair.getValue().map(f));
            }
            Command<A> fallback = getFallback().orElse(null);
            return new Mapping<>(newMap, fallback != null ? fallback.map(f) : null);
        }

        public Optional<Command<A>> getFallback() {
            return Optional.ofNullable(fallback);
        }
    }

    @Data
    @With
    class Parser<A> implements Command<A> {
        private final Function<List<String>, Tuple2<Option<A>, List<String>>> parser;
        private final List<Supplier<List<String>>> tabCompleters;
        private final List<MessageId> names;
        private final MessageId descriptionId;

        private Parser(Function<List<String>, Tuple2<Option<A>, List<String>>> parser, List<Supplier<List<String>>> tabCompleters, List<MessageId> names, MessageId descriptionId) {
            this.parser = parser;
            this.tabCompleters = tabCompleters;
            this.names = names;
            this.descriptionId = descriptionId;
        }

        @Override
        public <B> Parser<B> map(Function<? super A, ? extends B> f) {
            return new Parser<>(
                    args -> parser.apply(args).map1(aO -> aO.map(f)),
                    tabCompleters,
                    getNames(),
                    getDescriptionId()
            );
        }

        public Parser<A> withDescription(String description) {
            return withDescriptionId(MessageId.of("").withMessage(description));
        }
    }

    @SuppressWarnings("unchecked") // covariant
    @SafeVarargs
    static <A> Mapping<A> mapping(Tuple2<String, Command<? extends A>>... entries) {
        LinkedHashMap<String, Command<A>> map = new LinkedHashMap<>();
        for (Tuple2<String, Command<? extends A>> entry : entries) {
            map.put(entry._1(), (Command<A>) entry._2);
        }
        return new Mapping<>(map, null);
    }

    static <A> Parser<A> present(A value) {
        return argument(() -> value);
    }

    static <T> Parser<T> argument(Supplier<T> f) {
        return new Parser<>(
                args -> new Tuple2<>(Option.some(f.get()), args),
                Collections.emptyList(),
                Collections.singletonList(MessageId.of("")),
                MessageId.ofEmpty()
        );
    }

    static <T, A> Parser<T> argument(Function<? super A, ? extends T> f, Argument<A> argument) {
        return new Parser<>(
                args -> argument.getParser().apply(args).map1(aO -> Option.ofOptional(aO).map(f)),
                argument.getTabCompleters(),
                argument.getIds(),
                MessageId.of("")
        );
    }

    static <T, A, B> Parser<T> argument(Function2<? super A, ? super B, ? extends T> f, Argument<A> argA, Argument<B> argB) {
        return argument(tup -> f.apply(tup._1(), tup._2()), Argument.product(argA, argB));
    }

    static <T, A, B, C> Parser<T> argument(Function3<? super A, ? super B, ? super C, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC) {
        return argument(tup -> f.apply(tup._1(), tup._2(), tup._3()), Argument.product(argA, argB, argC));
    }

    static <T, A, B, C, D> Parser<T> argument(Function4<? super A, ? super B, ? super C, ? super D, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD) {
        return argument(tup -> f.apply(tup._1(), tup._2(), tup._3(), tup._4()), Argument.product(argA, argB, argC, argD));
    }

    static <T, A, B, C, D, E> Parser<T> argument(Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE) {
        return argument(tup -> f.apply(tup._1(), tup._2(), tup._3(), tup._4(), tup._5()), Argument.product(argA, argB, argC, argD, argE));
    }

    static <T, A, B, C, D, E, F> Parser<T> argument(Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF) {
        throw new UnsupportedOperationException("TODO");
    }

    static <T, A, B, C, D, E, F, G> Parser<T> argument(Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF, Argument<G> argG) {
        throw new UnsupportedOperationException("TODO");
    }

    static <K, V> Tuple2<K, V> pair(K key, V value) {
        return new Tuple2<>(key, value);
    }

    static <A> Either<CommandFailure<A>, CommandSuccess<A>> parse(String[] args, Command<A> command) {
        return parseWithIndex(0, args, command);
    }

    static <A> Optional<A> parseO(String[] args, Command<A> command) {
        return parse(args, command).toJavaOptional().map(CommandSuccess::getCommand);
    }

    static <A> Either<CommandFailure<A>, CommandSuccess<A>> parseWithIndex(int index, String[] args, Command<A> command) {
        String argument = args.length > index ? args[index] : null;
        if (command instanceof Command.Mapping) {
            Mapping<A> mapCommand = (Mapping<A>) command;
            if (argument == null) {
                Command<A> fallback = mapCommand.getFallback().orElse(null);
                return fallback != null
                        ? parseWithIndex(index + 1, args, fallback)
                        : Either.left(new CommandFailure.FewArguments<>(args, index, mapCommand));
            } else {
                Command<A> subCommand = mapCommand.getMap().get(argument);
                return subCommand != null
                        ? parseWithIndex(index + 1, args, subCommand)
                        : Either.left(new CommandFailure.UnknownSubCommand<>(args, index, mapCommand));
            }
        } else if (command instanceof Parser) {
            Parser<A> parser = (Parser<A>) command;
            List<String> list = index <= args.length
                    ? new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, index, args.length)))
                    : Collections.emptyList();
            Tuple2<Option<A>, List<String>> result = parser.getParser().apply(list);
            Option<A> aO = result._1;
            List<String> remainList = result._2;
            A a = aO.getOrNull();
            int currentIndex = index + list.size() - remainList.size();
            return aO.isDefined()
                    ? Either.right(new CommandSuccess<>(args, currentIndex, a))
                    : Either.left(new CommandFailure.ParsingFailure<>(parser.getNames(), parser));
        }
        throw new UnsupportedOperationException();
    }

    static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return tabCompleteWithIndex(0, args, command);
    }

    static <A> CommandTabResult<A> tabCompleteWithIndex(int index, String[] args, Command<A> command) {
        String argument = (args.length > index ? args[index] : "").toLowerCase();
        if (command instanceof Command.Mapping) {
            Mapping<A> mapCommand = (Mapping<A>) command;
            // if tail
            if (index >= args.length - 1) {
                return CommandTabResult.suggestion(
                        mapCommand.getMap().keySet().stream()
                                .filter(key -> key.toLowerCase().startsWith(argument))
                                .collect(Collectors.toList())
                );
            } else {
                Command<A> subCommand = mapCommand.getMap().get(argument);
                return subCommand != null
                        ? tabCompleteWithIndex(index + 1, args, subCommand)
                        : CommandTabResult.suggestion(Collections.emptyList());
            }
        } else if (command instanceof Parser) {
            Parser<A> parser = (Parser<A>) command;
            String lastArgument = args.length >= 1 ? args[args.length - 1] : "";
            int pos = args.length - index - 1;
            List<Supplier<List<String>>> tabCompleters = parser.getTabCompleters();
            Supplier<List<String>> tabCompleter = tabCompleters.size() > pos && pos >= 0
                    ? tabCompleters.get(pos)
                    : null;
            String lowerArgument = lastArgument.toLowerCase();
            List<String> tabComplete = tabCompleter != null
                    ? tabCompleter.get().stream()
                    .filter(s -> s.toLowerCase().startsWith(lowerArgument))
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            return CommandTabResult.suggestion(tabComplete);
        }
        return CommandTabResult.suggestion(Collections.emptyList());
    }

    static <A> List<Map.Entry<List<String>, Command<A>>> getEntries(Command<A> cmd) {
        if (cmd instanceof Command.Mapping) {
            Mapping<A> mapping = (Mapping<A>) cmd;
            return mapping.getMap().entrySet().stream()
                    .flatMap(pair -> {
                        List<Map.Entry<List<String>, Command<A>>> subEntries = getEntries(pair.getValue());
                        return subEntries.size() >= 1
                                ? subEntries.stream()
                                .map(subPair -> new AbstractMap.SimpleEntry<>(
                                        Stream.concat(
                                                Stream.of(pair.getKey()),
                                                subPair.getKey().stream()
                                        ).collect(Collectors.toList()),
                                        subPair.getValue()
                                ))
                                : Stream.of(new AbstractMap.SimpleEntry<>(Collections.singletonList(pair.getKey()), pair.getValue()));
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    static <A> CommandSpec getSpec(Command<A> cmd) {
        if (cmd instanceof Command.Parser) {
            Parser<A> parser = (Parser<A>) cmd;
            return CommandSpec.of(
                    parser.getNames(),
                    parser.getDescriptionId()
            );
        }
        return CommandSpec.empty;
    }
}
