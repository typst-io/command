package io.typecraft.command;

import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.Data;
import lombok.With;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
@Data(staticConstructor = "of")
@With
public class Argument<A> {
    private final List<String> names;
    private final Function<List<String>, Tuple2<Optional<A>, List<String>>> parser;
    private final List<Supplier<List<String>>> tabCompleters;
    public static final Argument<String> strArg =
            ofUnary("string", Optional::of, Collections::emptyList);
    public static final Argument<Integer> intArg =
            ofUnary("int", Argument::parseInt, Collections::emptyList);

    public static <A> Argument<A> ofUnary(
            String name,
            Function<String, Optional<A>> parser,
            Supplier<List<String>> tabCompleter
    ) {
        return of(
                Collections.singletonList(name),
                args -> {
                    List<String> newArgs = new ArrayList<>(args);
                    String arg = newArgs.size() >= 1 ? newArgs.remove(0) : "";
                    return new Tuple2<>(
                            arg.length() >= 1 ? parser.apply(arg) : Optional.<A>empty(),
                            newArgs
                    );
                },
                Collections.singletonList(tabCompleter)
        );
    }

    public Argument<A> withName(String name) {
        return withNames(Collections.singletonList(name));
    }

    public Argument<A> withTabCompleter(Supplier<List<String>> tabCompleter) {
        return withTabCompleters(Collections.singletonList(tabCompleter));
    }

    public static <A, B> Argument<Tuple2<A, B>> product(Argument<A> xa, Argument<B> xb) {
        return of(
                flatten(Stream.of(xa.getNames(), xb.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.map(b -> new Tuple2<>(a, b))), bPair._2);
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters()))
        );
    }

    public static <A, B, C> Argument<Tuple3<A, B, C>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc) {
        return new Argument<>(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.flatMap(b -> cPair._1.map(c -> new Tuple3<>(a, b, c)))), bPair._2);
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters()))
        );
    }

    private static <A> List<A> flatten(Stream<List<A>> xs) {
        return xs
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
