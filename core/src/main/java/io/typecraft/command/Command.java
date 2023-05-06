package io.typecraft.command;

import io.typecraft.command.algebra.Either;
import io.typecraft.command.algebra.Functor;
import io.typecraft.command.algebra.Option;
import io.typecraft.command.algebra.Tuple2;
import io.typecraft.command.function.*;
import io.typecraft.command.product.ArgumentProduct;
import lombok.Data;
import lombok.With;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public interface Command<A> {
    <B> Command<B> map(Function<? super A, ? extends B> f);

    @Data
    @With
    class Mapping<A> implements Command<A> {
        private final Map<String, Command<A>> commandMap;
        private final Command<A> fallback;

        private Mapping(Map<String, Command<A>> commandMap, @Nullable Command<A> fallback) {
            this.commandMap = commandMap;
            this.fallback = fallback;
        }

        @Override
        public <B> Mapping<B> map(Function<? super A, ? extends B> f) {
            Map<String, Command<B>> newMap = new HashMap<>(this.commandMap.size());
            for (Entry<String, Command<A>> pair : commandMap.entrySet()) {
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
        private final List<String> names;
        private final String description;
        private final String permission;

        private Parser(
                Function<List<String>, Tuple2<Option<A>, List<String>>> parser,
                List<Supplier<List<String>>> tabCompleters,
                List<String> names,
                String description,
                String permission
        ) {
            this.parser = parser;
            this.tabCompleters = tabCompleters;
            this.names = names;
            this.description = description;
            this.permission = permission;
        }

        @Override
        public <B> Parser<B> map(Function<? super A, ? extends B> f) {
            return new Parser<>(
                    args -> parser.apply(args).map1(aO -> Functor.map(aO, f)),
                    tabCompleters,
                    getNames(),
                    getDescription(),
                    getPermission()
            );
        }
    }

    @SuppressWarnings("unchecked") // covariant
    @SafeVarargs
    static <A> Mapping<A> mapping(Tuple2<String, Command<? extends A>>... entries) {
        LinkedHashMap<String, Command<A>> map = new LinkedHashMap<>();
        for (Tuple2<String, Command<? extends A>> entry : entries) {
            map.put(entry.getA(), (Command<A>) entry.getB());
        }
        return new Mapping<>(map, null);
    }

    static <A> Parser<A> present(A value) {
        return argument(() -> value);
    }

    static <T> Parser<T> argument(Supplier<T> f) {
        return new Parser<>(
                args -> new Tuple2<>(new Option.Some<>(f.get()), args),
                Collections.emptyList(),
                singletonList(""),
                "",
                ""
        );
    }

    static <T, A> Parser<T> argument(Function<? super A, ? extends T> f, Argument<A> argument) {
        return new Parser<>(
                args -> argument.getParser().apply(args).map1(a -> Functor.map(Option.from(a), f)),
                argument.getTabCompleters(),
                argument.getNames(),
                "",
                ""
        );
    }

    static <T, A, B> Parser<T> argument(BiFunction<? super A, ? super B, ? extends T> f, Argument<A> argA, Argument<B> argB) {
        return argument(tup -> f.apply(tup.getA(), tup.getB()), ArgumentProduct.product(argA, argB));
    }

    static <T, A, B, C> Parser<T> argument(Function3<? super A, ? super B, ? super C, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC) {
        return argument(tup -> f.apply(tup.getA(), tup.getB(), tup.getC()), ArgumentProduct.product(argA, argB, argC));
    }

    static <T, A, B, C, D> Parser<T> argument(Function4<? super A, ? super B, ? super C, ? super D, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD) {
        return argument(tup -> f.apply(tup.getA(), tup.getB(), tup.getC(), tup.getD()), ArgumentProduct.product(argA, argB, argC, argD));
    }

    static <T, A, B, C, D, E> Parser<T> argument(Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE) {
        return argument(tup -> f.apply(tup.getA(), tup.getB(), tup.getC(), tup.getD(), tup.getE()), ArgumentProduct.product(argA, argB, argC, argD, argE));
    }

    static <T, A, B, C, D, E, F> Parser<T> argument(Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF) {
        return argument(tup -> f.apply(tup.getA(), tup.getB(), tup.getC(), tup.getD(), tup.getE(), tup.getF()), ArgumentProduct.product(argA, argB, argC, argD, argE, argF));
    }

    static <T, A, B, C, D, E, F, G> Parser<T> argument(Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF, Argument<G> argG) {
        return argument(tup -> f.apply(tup.getA(), tup.getB(), tup.getC(), tup.getD(), tup.getE(), tup.getF(), tup.getG()), ArgumentProduct.product(argA, argB, argC, argD, argE, argF, argG));
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
                        : new Either.Left<>(new CommandFailure.FewArguments<>(args, index, mapCommand));
            } else {
                Command<A> subCommand = mapCommand.getCommandMap().get(argument);
                return subCommand != null
                        ? parseWithIndex(index + 1, args, subCommand)
                        : new Either.Left<>(new CommandFailure.UnknownSubCommand<>(args, index, mapCommand));
            }
        } else if (command instanceof Parser) {
            Parser<A> parser = (Parser<A>) command;
            List<String> list = index <= args.length
                    ? new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, index, args.length)))
                    : Collections.emptyList();
            Tuple2<Option<A>, List<String>> result = parser.getParser().apply(list);
            Option<A> aO = result.getA();
            List<String> remainList = result.getB();
            A a = aO.getOrNull();
            int currentIndex = index + list.size() - remainList.size();
            return aO.isDefined()
                    ? new Either.Right<>(new CommandSuccess<>(args, currentIndex, a))
                    : new Either.Left<>(new CommandFailure.ParsingFailure<>(parser.getNames(), parser));
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
                        mapCommand.getCommandMap().keySet().stream()
                                .filter(key -> key.toLowerCase().startsWith(argument))
                                .collect(Collectors.toList())
                );
            } else {
                Command<A> subCommand = mapCommand.getCommandMap().get(argument);
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

    static <A> List<Entry<List<String>, Command<A>>> getEntries(Command<A> cmd) {
        if (cmd instanceof Command.Mapping) {
            Mapping<A> mapping = (Mapping<A>) cmd;
            return mapping.getCommandMap().entrySet().stream()
                    .flatMap(pair -> {
                        List<Entry<List<String>, Command<A>>> subEntries = getEntries(pair.getValue());
                        return subEntries.size() >= 1
                                ? subEntries.stream()
                                .map(subPair -> new SimpleEntry<>(
                                        Stream.concat(
                                                Stream.of(pair.getKey()),
                                                subPair.getKey().stream()
                                        ).collect(Collectors.toList()),
                                        subPair.getValue()
                                ))
                                : Stream.of(new SimpleEntry<>(singletonList(pair.getKey()), pair.getValue()));
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
                    parser.getDescription(),
                    parser.getPermission()
            );
        }
        return CommandSpec.empty;
    }
}
