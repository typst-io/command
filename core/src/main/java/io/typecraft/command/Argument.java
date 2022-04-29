package io.typecraft.command;

import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.Data;
import lombok.With;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
@Data
@With
public class Argument<A> {
    private final List<String> names;
    private final Function<Iterator<String>, Optional<A>> parser;

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
                flatten(Stream.of(xa.getNames(), xb.getNames())),
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
}
