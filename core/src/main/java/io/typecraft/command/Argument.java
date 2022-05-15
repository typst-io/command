package io.typecraft.command;

import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.Data;
import lombok.With;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
@Data
@With
public class Argument<A> {
    private final List<String> names;
    private final Function<Iterator<String>, Optional<A>> parser;
    public static final Argument<String> strArg =
            of("string", args -> args.hasNext() ? Optional.of(args.next()) : Optional.empty());
    public static final Argument<Integer> intArg =
            of("int", args -> args.hasNext() ? parseInt(args.next()) : Optional.empty());

    public static <A> Argument<A> of(String name, Function<Iterator<String>, Optional<A>> parser) {
        return new Argument<>(Collections.singletonList(name), parser);
    }

    public Argument<A> withName(String name) {
        return withNames(Collections.singletonList(name));
    }

    public static <A, B> Argument<Tuple2<A, B>> product(Argument<A> xa, Argument<B> xb) {
        return new Argument<>(
                flatten(Stream.of(xa.getNames(), xb.getNames())),
                strs -> {
                    Optional<A> aO = xa.getParser().apply(strs);
                    Optional<B> bO = xb.getParser().apply(strs);
                    return aO.flatMap(a -> bO.map(b -> new Tuple2<>(a, b)));
                }
        );
    }

    public static <A, B, C> Argument<Tuple3<A, B, C>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc) {
        return new Argument<>(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames())),
                strs -> {
                    Optional<A> aO = xa.getParser().apply(strs);
                    Optional<B> bO = xb.getParser().apply(strs);
                    Optional<C> cO = xc.getParser().apply(strs);
                    return aO.flatMap(a -> bO.flatMap(b -> cO.map(c -> new Tuple3<>(a, b, c))));
                }
        );
    }

    private static List<String> flatten(Stream<List<String>> xs) {
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
