package io.typecraft.command;

import io.vavr.*;
import io.vavr.control.Either;
import lombok.Data;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Command<A> {
    @Data
    class Mapping<A> implements Command<A> {
        // TODO: i18n?
        private final Map<String, Command<A>> map;

        private Mapping(Map<String, Command<A>> map) {
            this.map = map;
        }
    }

    @Data
    class Present<A> implements Command<A> {
        private final A value;

        private Present(A value) {
            this.value = value;
        }
    }

    @Data
    class Parser<A> implements Command<A> {
        private final Function<Iterator<String>, Optional<A>> parser;
        private final List<String> names;
    }

    @SafeVarargs
    static <A> Command<A> map(Tuple2<String, Command<A>>... entries) {
        LinkedHashMap<String, Command<A>> map = new LinkedHashMap<>();
        for (Tuple2<String, Command<A>> entry : entries) {
            map.put(entry._1(), entry._2());
        }
        return new Mapping<>(map);
    }

    static <A> Command<A> present(A value) {
        return new Present<>(value);
    }

    static <T, A> Command<T> argument(Function<A, T> f, Argument<A> argument) {
        return new Parser<>(
                strs -> argument.getParser().apply(strs).map(f),
                argument.getNames()
        );
    }

    static <T, A, B> Command<T> argument(Function2<A, B, T> f, Argument<A> argA, Argument<B> argB) {
        return argument(tup -> f.apply(tup._1(), tup._2()), Argument.product(argA, argB));
    }

    static <T, A, B, C> Command<T> argument(Function3<A, B, C, T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC) {
        return argument(tup -> f.apply(tup._1(), tup._2(), tup._3()), Argument.product(argA, argB, argC));
    }

    static <T, A, B, C, D> Command<T> argument(Function4<A, B, C, D, T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD) {
        throw new UnsupportedOperationException("TODO");
    }

    static <T, A, B, C, D, E> Command<T> argument(Function5<A, B, C, D, E, T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE) {
        throw new UnsupportedOperationException("TODO");
    }

    static <T, A, B, C, D, E, F> Command<T> argument(Function6<A, B, C, D, E, F, T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF) {
        throw new UnsupportedOperationException("TODO");
    }

    static <T, A, B, C, D, E, F, G> Command<T> argument(Function7<A, B, C, D, E, F, G, T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF, Argument<G> argG) {
        throw new UnsupportedOperationException("TODO");
    }

    static <K, V> Tuple2<K, V> pair(K key, V value) {
        return new Tuple2<>(key, value);
    }

    static <A> Either<CommandFailure, CommandSuccess<A>> parse(String[] args, Command<A> command) {
        return parseWithIndex(0, args, command);
    }

    static <A> Either<CommandFailure, CommandSuccess<A>> parseWithIndex(int index, String[] args, Command<A> command) {
        String argument = args.length > index ? args[index] : null;
        if (command instanceof Mapping) {
            if (argument == null) {
                return Either.left(new CommandFailure.FewArguments(args, index));
            }
            Mapping<A> mapCommand = (Mapping<A>) command;
            Command<A> subCommand = mapCommand.getMap().get(argument);
            return subCommand != null
                    ? parseWithIndex(index + 1, args, subCommand)
                    : Either.left(new CommandFailure.UnknownSubCommand(args, index));
        } else if (command instanceof Present) {
            Present<A> present = (Present<A>) command;
            return Either.right(new CommandSuccess<>(args, index, present.getValue()));
        } else if (command instanceof Parser) {
            Parser<A> parser = (Parser<A>) command;
            List<String> list = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, index, args.length)));
            A a = parser.getParser().apply(list.iterator()).orElse(null);
            return a != null
                    ? Either.right(new CommandSuccess<>(new String[0], args.length, a))
                    : Either.left(new CommandFailure.ParsingFailure(parser.getNames()));
        }
        throw new UnsupportedOperationException();
    }

    static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return tabCompleteWithIndex(0, args, command);
    }

    static <A> CommandTabResult<A> tabCompleteWithIndex(int index, String[] args, Command<A> command) {
        String argument = (args.length > index ? args[index] : "").toLowerCase();
        if (command instanceof Mapping) {
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
        } else if (command instanceof Present) {
            Present<A> present = (Present<A>) command;
            String[] newArgs = Arrays.copyOfRange(args, index, args.length);
            return CommandTabResult.present(newArgs, present.getValue());
        }
        return CommandTabResult.suggestion(Collections.emptyList());
    }
}
