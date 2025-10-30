package io.typst.command;

import io.typst.command.algebra.Either;
import io.typst.command.algebra.Functor;
import io.typst.command.algebra.Option;
import io.typst.command.algebra.Tuple2;
import io.typst.command.function.*;
import lombok.Data;
import lombok.With;
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

/**
 * This is a sum type to represent a command structure.
 * <p>{@link Command} = {@link Mapping} or {@link Parser}</p>
 *
 * <p>Constructors: Command.mapping(), Command.argument()</p>
 *
 * <p>This commands:</p>
 *
 * <pre>
 *     /label concrete &lt;string&gt;
 *     /label map a
 *     /label map b &lt;string&gt;
 * </pre>
 * <p>can be represented:</p>
 * <pre>
 *     {@code
 *     interface MyCommand {
 *         record Concrete(String s) implements MyCommand {}
 *         class A implements MyCommand {}
 *         record B(String s) implements MyCommand {}
 *     }
 *
 *     Command.mapping(
 *      pair("concrete", Command.argument(Concrete::new, strArg),
 *      pair("map", Command.mapping(
 *          pair("a", Command.argument(A::new),
 *          pair("b", Command.argument(B::new, strArg)
 *      )
 *     )
 *     }
 * </pre>
 *
 * <pre>Please note that the MyCommand is a sum type in other word such an enum with their individual fields, also called sealed class in Kotlin.</pre>
 *
 * @param <A> The command of their business logic
 * @see <a href="https://cguntur.me/2021/01/12/algebraic-in-java-part-1/">Algebraic - in Java?</a>
 * @see Command.Mapping
 * @see Command.Parser
 */
public interface Command<A> {
    <B> Command<B> map(Function<? super A, ? extends B> f);


    /**
     * Represents an abstract command with key-value pairs.
     *
     * <p>Constructor: Command.mapping(pairs)</p>
     *
     * <p>A handy constructor for pairs: Command.pair(a, b)</p>
     *
     * <p>This commands:</p>
     *
     * <pre>
     * /label a
     * /label b
     * </pre>
     * <p>
     * can be represented:
     *
     * <pre>
     *     {@code
     *     enum MyCommand {
     *         A, B
     *     }
     *
     *     Command.mapping(
     *      pair("a", Command.present { MyCommand.A }),
     *      pair("b", Command.present { MyCommand.B }),
     *     );
     *     }
     * </pre>
     *
     * @param <A> the command of their business logic
     * @see Command.Parser
     * @see Command
     */
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

    /**
     * Represents a concrete command with type-safe arguments.
     *
     * <p>This command:</p>
     *
     * <pre>
     *     /label &lt;int&gt; &lt;string&gt;
     * </pre>
     * <p>
     * can be represented:
     *
     * <pre>
     *     {@code
     *     record MyCommand(Integer a, String b)
     *
     *     Command.argument(MyCommand::new, intArg, strArg);
     *     }
     * </pre>
     *
     * @param <A> The command of their business logic
     * @see Command.Mapping
     * @see Command
     */
    @Data
    @With
    class Parser<A> implements Command<A> {
        private final Function<List<String>, Tuple2<Option<A>, List<String>>> parser;
        private final List<Argument<?>> arguments;
        private final String description;
        private final String permission;

        public Parser(Function<List<String>, Tuple2<Option<A>, List<String>>> parser, List<Argument<?>> arguments, String description, String permission) {
            this.parser = parser;
            this.arguments = arguments;
            this.description = description;
            this.permission = permission;
        }

        @Override
        public <B> Parser<B> map(Function<? super A, ? extends B> f) {
            return new Parser<>(
                    args -> parser.apply(args).map1(aO -> Functor.map(aO, f)),
                    arguments,
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
                "",
                ""
        );
    }

    static <T, A> Parser<T> argument(Function<? super A, ? extends T> f, Argument<A> argument) {
        return new Parser<>(
                args -> argument.getParser().apply(args).map1(a -> Functor.map(Option.from(a), f)),
                singletonList(argument),
                "",
                ""
        );
    }

    static <T, A, B> Parser<T> argument(BiFunction<? super A, ? super B, ? extends T> f, Argument<A> argA, Argument<B> argB) {
        return new Parser<>(
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = argA.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = argB.getParser().apply(aPair.getB());
                    return new Tuple2<>(Option.from(aPair.getA().flatMap(a -> bPair.getA().map(b -> f.apply(a, b)))), bPair.getB());
                },
                Arrays.asList(argA, argB),
                "",
                ""
        );
    }

    static <T, A, B, C> Parser<T> argument(Function3<? super A, ? super B, ? super C, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC) {
        return new Parser<>(
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = argA.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = argB.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = argC.getParser().apply(bPair.getB());
                    return new Tuple2<>(Option.from(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().map(c -> f.apply(a, b, c))))), cPair.getB());
                },
                Arrays.asList(argA, argB, argC),
                "",
                ""
        );
    }

    static <T, A, B, C, D> Parser<T> argument(Function4<? super A, ? super B, ? super C, ? super D, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD) {
        return new Parser<>(
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = argA.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = argB.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = argC.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = argD.getParser().apply(cPair.getB());
                    return new Tuple2<>(Option.from(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().map(d -> f.apply(a, b, c, d)))))), dPair.getB());
                },
                Arrays.asList(argA, argB, argC, argD),
                "",
                ""
        );
    }

    static <T, A, B, C, D, E> Parser<T> argument(Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE) {
        return new Parser<>(
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = argA.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = argB.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = argC.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = argD.getParser().apply(cPair.getB());
                    Tuple2<Optional<E>, List<String>> ePair = argE.getParser().apply(dPair.getB());
                    return new Tuple2<>(Option.from(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().flatMap(d -> ePair.getA().map(e -> f.apply(a, b, c, d, e))))))), ePair.getB());
                },
                Arrays.asList(argA, argB, argC, argD, argE),
                "",
                ""
        );
    }

    static <T, A, B, C, D, E, F> Parser<T> argument(Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF) {
        return new Parser<>(
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = argA.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = argB.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = argC.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = argD.getParser().apply(cPair.getB());
                    Tuple2<Optional<E>, List<String>> ePair = argE.getParser().apply(dPair.getB());
                    Tuple2<Optional<F>, List<String>> fPair = argF.getParser().apply(ePair.getB());
                    return new Tuple2<>(Option.from(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().flatMap(d -> ePair.getA().flatMap(e -> fPair.getA().map(fv -> f.apply(a, b, c, d, e, fv)))))))), fPair.getB());
                },
                Arrays.asList(argA, argB, argC, argD, argE, argF),
                "",
                ""
        );
    }

    static <T, A, B, C, D, E, F, G> Parser<T> argument(Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends T> f, Argument<A> argA, Argument<B> argB, Argument<C> argC, Argument<D> argD, Argument<E> argE, Argument<F> argF, Argument<G> argG) {
        return new Parser<>(
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = argA.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = argB.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = argC.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = argD.getParser().apply(cPair.getB());
                    Tuple2<Optional<E>, List<String>> ePair = argE.getParser().apply(dPair.getB());
                    Tuple2<Optional<F>, List<String>> fPair = argF.getParser().apply(ePair.getB());
                    Tuple2<Optional<G>, List<String>> gPair = argG.getParser().apply(fPair.getB());
                    return new Tuple2<>(Option.from(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().flatMap(d -> ePair.getA().flatMap(e -> fPair.getA().flatMap(fv -> gPair.getA().map(g -> f.apply(a, b, c, d, e, fv, g))))))))), gPair.getB());
                },
                Arrays.asList(argA, argB, argC, argD, argE, argF, argG),
                "",
                ""
        );
    }

    static <K, V> Tuple2<K, V> pair(K key, V value) {
        return new Tuple2<>(key, value);
    }

    static <A> Either<CommandFailure<A>, CommandSuccess<A>> parse(String[] args, Command<A> command) {
        return parseWithIndex(0, args, command);
    }

    @SuppressWarnings("unused")
    static <A> Optional<A> parseO(String[] args, Command<A> command) {
        return parse(args, command).toJavaOptional().map(CommandSuccess::getCommand);
    }

    static <A> Either<CommandFailure<A>, CommandSuccess<A>> parseWithIndex(int index, String[] args, Command<A> command) {
        String argument = args.length > index ? args[index] : null;
        if (command instanceof Command.Mapping) {
            Mapping<A> mapCommand = (Mapping<A>) command;

            Command<A> subCommand = mapCommand.getCommandMap().get(argument);
            if (subCommand != null) {
                return parseWithIndex(index + 1, args, subCommand);
            } else if (argument != null) {
                return new Either.Left<>(new CommandFailure.UnknownSubCommand<>(args, index, mapCommand));
            } else {
                Command<A> fallback = mapCommand.getFallback().orElse(null);
                if (fallback != null) {
                    return parseWithIndex(index, args, fallback);
                } else {
                    return new Either.Left<>(new CommandFailure.FewArguments<>(args, index, mapCommand));
                }
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
                    ? new Either.Right<>(new CommandSuccess<>(args, currentIndex, a, parser))
                    : new Either.Left<>(new CommandFailure.ParsingFailure<>(args, index, parser, parser.getArguments()));
        }
        throw new UnsupportedOperationException();
    }

    static <A> CommandTabResult<A> tabComplete(String[] args, Command<A> command) {
        return tabComplete(new CommandSource(""), args, command);
    }

    static <A> CommandTabResult<A> tabComplete(CommandSource source, String[] args, Command<A> command) {
        return tabCompleteWithIndex(0, source, args, command);
    }

    static <A> CommandTabResult<A> tabCompleteWithIndex(int index, CommandSource source, String[] args, Command<A> command) {
        String arg = args.length > index ? args[index] : "";
        String arglc = arg.toLowerCase();
        if (command instanceof Command.Mapping) {
            Mapping<A> mapCommand = (Mapping<A>) command;
            // if tail
            if (index >= args.length - 1) {
                return new CommandTabResult.Suggestions<>(
                        mapCommand.getCommandMap().entrySet().stream()
                                .filter(pair -> pair.getKey().toLowerCase().startsWith(arglc))
                                .map(pair -> new Tuple2<>(pair.getKey(), Optional.of(pair.getValue())))
                                .collect(Collectors.toList())
                );
            } else {
                Command<A> subCommand = mapCommand.getCommandMap().get(arg);
                return subCommand != null
                        ? tabCompleteWithIndex(index + 1, source, args, subCommand)
                        : new CommandTabResult.Suggestions<>(Collections.emptyList());
            }
        } else if (command instanceof Parser) {
            Parser<A> parser = (Parser<A>) command;
            String lastArgument = args.length >= 1 ? args[args.length - 1] : "";
            int pos = args.length - index - 1;
            List<Argument<?>> arguments = parser.getArguments();
            Function<ParseContext, List<String>> tabCompleter = arguments.size() > pos && pos >= 0
                    ? arguments.get(pos).getContextualTabCompleter()
                    : null;
            String lowerArgument = lastArgument.toLowerCase();
            List<Tuple2<String, Optional<Command<A>>>> tabCompletes = tabCompleter != null
                    ? tabCompleter.apply(new ParseContext(source, Arrays.asList(args))).stream()
                    .filter(s -> s.toLowerCase().startsWith(lowerArgument))
                    .map(s -> new Tuple2<>(s, Optional.of(command)))
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            return new CommandTabResult.Suggestions<>(tabCompletes);
        }
        return new CommandTabResult.Suggestions<>(Collections.emptyList());
    }

    /**
     * Make flatten commands.
     *
     * <p>Example input:</p>
     *
     * <pre>
     *     <code>{
     *         a: {
     *             a: cmdA,
     *             b: cmdB
     *         }
     *     }</code>
     * </pre>
     *
     * <p>result:</p>
     *
     * <pre>
     *     <code>[
     *      (["a", "a"], cmdA),
     *      (["a", "b"], cmdB),
     *      ...
     *     ]</code>
     * </pre>
     *
     * @param cmd the node
     * @param <A> the command of their business logic
     * @return flatten commands
     */
    static <A> List<Entry<List<String>, Command<A>>> getEntries(Command<A> cmd) {
        if (cmd instanceof Command.Mapping) {
            Mapping<A> mapping = (Mapping<A>) cmd;
            return mapping.getCommandMap().entrySet().stream()
                    .flatMap(pair -> {
                        String key = pair.getKey();
                        Command<A> subCmd = pair.getValue();
                        List<Entry<List<String>, Command<A>>> subEntries = getEntries(subCmd);
                        return !subEntries.isEmpty()
                                ? subEntries.stream()
                                .map(subPair -> {
                                    List<String> keys = new ArrayList<>();
                                    keys.add(key);
                                    keys.addAll(subPair.getKey());
                                    return new SimpleEntry<>(
                                            keys,
                                            subPair.getValue()
                                    );
                                })
                                : Stream.of(new SimpleEntry<>(singletonList(key), subCmd));
                    })
                    .collect(Collectors.toList());
        } else if (cmd instanceof Command.Parser) {
//            Parser<A> parser = (Parser<A>) cmd;
            return Collections.singletonList(new SimpleEntry<>(Collections.emptyList(), cmd));
        }
        return Collections.emptyList();
    }
}
