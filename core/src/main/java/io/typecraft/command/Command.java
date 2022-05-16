package io.typecraft.command;

import io.vavr.*;
import io.vavr.control.Either;
import lombok.Data;
import lombok.With;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Command<A> {
    <B> Command<B> map(Function<? super A, ? extends B> f);

    @Data
    class Compound<A> implements Command<A> {
        // TODO: i18n?
        private final Map<String, Command<A>> map;

        private Compound(Map<String, Command<A>> map) {
            this.map = map;
        }

        @Override
        public <B> Command<B> map(Function<? super A, ? extends B> f) {
            Map<String, Command<B>> newMap = new HashMap<>(this.map.size());
            for (Map.Entry<String, Command<A>> pair : map.entrySet()) {
                newMap.put(pair.getKey(), pair.getValue().map(f));
            }
            return new Compound<>(newMap);
        }
    }

    @Data
    @With
    class Present<A> implements Command<A> {
        private final A value;
        private final String description;

        private Present(A value, String description) {
            this.value = value;
            this.description = description;
        }

        @Override
        public <B> Command<B> map(Function<? super A, ? extends B> f) {
            return new Present<>(f.apply(value), getDescription());
        }
    }

    @Data
    @With
    class Parser<A> implements Command<A> {
        private final Function<List<String>, Tuple2<Optional<A>, List<String>>> parser;
        private final List<Supplier<List<String>>> tabCompleters;
        private final List<String> names;
        private final String description;

        private Parser(Function<List<String>, Tuple2<Optional<A>, List<String>>> parser, List<Supplier<List<String>>> tabCompleters, List<String> names, String description) {
            this.parser = parser;
            this.tabCompleters = tabCompleters;
            this.names = names;
            this.description = description;
        }

        @Override
        public <B> Command<B> map(Function<? super A, ? extends B> f) {
            return new Parser<>(
                    args -> parser.apply(args).map1(aO -> aO.map(f)),
                    tabCompleters,
                    getNames(),
                    getDescription()
            );
        }
    }

    @SuppressWarnings("unchecked") // covariant
    @SafeVarargs
    static <A> Compound<A> compound(Tuple2<String, Command<? extends A>>... entries) {
        LinkedHashMap<String, Command<A>> map = new LinkedHashMap<>();
        for (Tuple2<String, Command<? extends A>> entry : entries) {
            map.put(entry._1(), (Command<A>) entry._2);
        }
        return new Compound<>(map);
    }

    static <A> Present<A> present(A value) {
        return new Present<>(value, "");
    }

    static <T, A> Parser<T> argument(Function<? super A, ? extends T> f, Argument<A> argument) {
        return new Parser<>(
                args -> argument.getParser().apply(args).map1(aO -> aO.map(f)),
                argument.getTabCompleters(),
                argument.getNames(),
                ""
        );
    }

    static <T, A, B> Command<T> argument(Function2<? super A, ? super B, ? extends T> f, Argument<A> argA, Argument<B> argB) {
        return argument(tup -> f.apply(tup._1(), tup._2()), Argument.product(argA, argB));
    }

    static <T, A, B, C> Command<T> argument(Function3<? super A, ? super B, ? super C, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC) {
        return argument(tup -> f.apply(tup._1(), tup._2(), tup._3()), Argument.product(argA, argB, argC));
    }

    static <T, A, B, C, D> Command<T> argument(Function4<? super A, ? super B, ? super C, ? super D, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD) {
        throw new UnsupportedOperationException("TODO");
    }

    static <T, A, B, C, D, E> Command<T> argument(Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE) {
        throw new UnsupportedOperationException("TODO");
    }

    static <T, A, B, C, D, E, F> Command<T> argument(Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF) {
        throw new UnsupportedOperationException("TODO");
    }

    static <T, A, B, C, D, E, F, G> Command<T> argument(Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF, Argument<G> argG) {
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
        if (command instanceof Command.Compound) {
            Compound<A> mapCommand = (Compound<A>) command;
            if (argument == null) {
                return Either.left(new CommandFailure.FewArguments<>(args, index, mapCommand));
            }
            Command<A> subCommand = mapCommand.getMap().get(argument);
            return subCommand != null
                    ? parseWithIndex(index + 1, args, subCommand)
                    : Either.left(new CommandFailure.UnknownSubCommand<>(args, index, mapCommand));
        } else if (command instanceof Present) {
            Present<A> present = (Present<A>) command;
            return Either.right(new CommandSuccess<>(args, index, present.getValue()));
        } else if (command instanceof Parser) {
            Parser<A> parser = (Parser<A>) command;
            List<String> list = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, index, args.length)));
            A a = parser.getParser().apply(list)._1.orElse(null);
            return a != null
                    ? Either.right(new CommandSuccess<>(new String[0], args.length, a))
                    : Either.left(new CommandFailure.ParsingFailure<>(parser.getNames(), parser));
        }
        throw new UnsupportedOperationException();
    }

    static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return tabCompleteWithIndex(0, args, command);
    }

    static <A> CommandTabResult<A> tabCompleteWithIndex(int index, String[] args, Command<A> command) {
        String argument = (args.length > index ? args[index] : "").toLowerCase();
        if (command instanceof Command.Compound) {
            Compound<A> mapCommand = (Compound<A>) command;
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
        } else if (command instanceof Present) {
            Present<A> present = (Present<A>) command;
            String[] newArgs = Arrays.copyOfRange(args, index, args.length);
            return CommandTabResult.present(newArgs, present.getValue());
        } else if (command instanceof Parser) {
            Parser<A> parser = (Parser<A>) command;
            int pos = args.length - index - 1;
            List<Supplier<List<String>>> tabCompleters = parser.getTabCompleters();
            Supplier<List<String>> tabCompleter = tabCompleters.size() > pos && pos >= 0
                    ? tabCompleters.get(pos)
                    : null;
            String lowerArgument = argument.toLowerCase();
            List<String> tabComplete = tabCompleter != null
                    ? tabCompleter.get().stream()
                    .filter(s -> s.startsWith(lowerArgument))
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            return CommandTabResult.suggestion(tabComplete);
        }
        return CommandTabResult.suggestion(Collections.emptyList());
    }

    static <A> List<Map.Entry<List<String>, Command<A>>> getEntries(Command<A> cmd) {
        if (cmd instanceof Command.Compound) {
            Command.Compound<A> compound = (Command.Compound<A>) cmd;
            return compound.getMap().entrySet().stream()
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
        if (cmd instanceof Command.Present) {
            Present<A> present = (Present<A>) cmd;
            return CommandSpec.of(
                    Collections.emptyList(),
                    present.getDescription()
            );
        } else if (cmd instanceof Command.Parser) {
            Parser<A> parser = (Parser<A>) cmd;
            return CommandSpec.of(
                    parser.getNames(),
                    parser.getDescription()
            );
        }
        return CommandSpec.empty;
    }
}
