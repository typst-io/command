package io.typst.command;

import io.typst.command.algebra.Tuple2;
import lombok.Value;
import lombok.With;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("DuplicatedCode")
@Value
@With
public class Argument<A> {
    String name;
    Class<?> classType;
    Function<List<String>, Tuple2<Optional<A>, List<String>>> parser;
    Function<ParseContext, List<String>> contextualTabCompleter;
    /** Whether the parser accepts an empty token list. */
    boolean optional;
    /** Whether the parser consumes all remaining tokens. Such an argument must be last in Brigadier. */
    boolean greedy;

    private Argument(String name, Class<?> classType,
                     Function<List<String>, Tuple2<Optional<A>, List<String>>> parser,
                     Function<ParseContext, List<String>> contextualTabCompleter,
                     boolean optional, boolean greedy) {
        this.name = name;
        this.classType = classType;
        this.parser = parser;
        this.contextualTabCompleter = contextualTabCompleter;
        this.optional = optional;
        this.greedy = greedy;
    }

    public static <A> Argument<A> ofContext(
            String name, Class<?> classType,
            Function<List<String>, Tuple2<Optional<A>, List<String>>> parser,
            Function<ParseContext, List<String>> contextualTabCompleter
    ) {
        return new Argument<>(name, classType, parser, contextualTabCompleter, false, classType == List.class);
    }

    public static <A> Argument<A> ofUnary(
            String name,
            Class<?> classType,
            Function<String, Optional<A>> parser,
            Supplier<List<String>> tabCompleter
    ) {
        return Argument.<A>of(
                name,
                classType,
                args -> {
                    List<String> newArgs = new LinkedList<>(args);
                    String arg = newArgs.size() >= 1 ? newArgs.remove(0) : "";
                    return new Tuple2<>(
                            arg.length() >= 1 ? parser.apply(arg) : Optional.empty(),
                            newArgs
                    );
                },
                tabCompleter
        ).withGreedy(false);
    }

    public static <A> Argument<A> of(
            String name,
            Class<?> classType,
            Function<List<String>, Tuple2<Optional<A>, List<String>>> parser,
            Supplier<List<String>> tabCompletes
    ) {
        return ofContext(name, classType, parser, ctx -> tabCompletes.get());
    }

    public Argument<A> withTabCompletes(Supplier<List<String>> f) {
        return withContextualTabCompleter(ctx -> f.get());
    }

    public Argument<Optional<A>> asOptional() {
        return new Argument<>(
                name,
                classType,
                args -> {
                    Tuple2<Optional<A>, List<String>> ret = getParser().apply(args);
                    return args.isEmpty()
                            ? ret.map1(Optional::of)
                            : ret.map1(aO -> aO.map(Optional::of));
                },
                getContextualTabCompleter(),
                true,
                greedy
        );
    }

    public <B> Argument<B> map(Function<A, B> f) {
        return new Argument<>(
                name,
                classType,
                args -> {
                    Tuple2<Optional<A>, List<String>> pair = getParser().apply(args);
                    return pair.map1(a -> a.map(f));
                },
                getContextualTabCompleter(),
                optional,
                greedy
        );
    }
}
