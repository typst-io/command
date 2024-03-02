package io.typst.command.product;

import io.typst.command.Argument;
import io.typst.command.algebra.*;
import io.typst.command.algebra.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgumentProduct {
    public static <A, B> Argument<Tuple2<A, B>> product(Argument<A> xa, Argument<B> xb) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair.getB());
                    return new Tuple2<>(aPair.getA().flatMap(a -> bPair.getA().map(b -> new Tuple2<>(a, b))), bPair.getB());
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters()))
        );
    }

    public static <A, B, C> Argument<Tuple3<A, B, C>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair.getB());
                    return new Tuple2<>(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().map(c -> new Tuple3<>(a, b, c)))), cPair.getB());
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters()))
        );
    }

    public static <A, B, C, D> Argument<Tuple4<A, B, C, D>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair.getB());
                    return new Tuple2<>(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().map(d -> new Tuple4<>(a, b, c, d))))), dPair.getB());
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters(), xd.getTabCompleters()))
        );
    }

    public static <A, B, C, D, E> Argument<Tuple5<A, B, C, D, E>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd, Argument<E> xe) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames(), xe.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair.getB());
                    Tuple2<Optional<E>, List<String>> ePair = xe.getParser().apply(dPair.getB());
                    return new Tuple2<>(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().flatMap(d -> ePair.getA().map(e -> new Tuple5<>(a, b, c, d, e)))))), ePair.getB());
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters(), xd.getTabCompleters(), xe.getTabCompleters()))
        );
    }

    public static <A, B, C, D, E, F> Argument<Tuple6<A, B, C, D, E, F>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd, Argument<E> xe, Argument<F> xf) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames(), xe.getNames(), xf.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair.getB());
                    Tuple2<Optional<E>, List<String>> ePair = xe.getParser().apply(dPair.getB());
                    Tuple2<Optional<F>, List<String>> fPair = xf.getParser().apply(ePair.getB());
                    return new Tuple2<>(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().flatMap(d -> ePair.getA().flatMap(e -> fPair.getA().map(f -> new Tuple6<>(a, b, c, d, e, f))))))), fPair.getB());
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters(), xd.getTabCompleters(), xe.getTabCompleters(), xf.getTabCompleters()))
        );
    }

    public static <A, B, C, D, E, F, G> Argument<Tuple7<A, B, C, D, E, F, G>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd, Argument<E> xe, Argument<F> xf, Argument<G> xg) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames(), xe.getNames(), xf.getNames(), xg.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair.getB());
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair.getB());
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair.getB());
                    Tuple2<Optional<E>, List<String>> ePair = xe.getParser().apply(dPair.getB());
                    Tuple2<Optional<F>, List<String>> fPair = xf.getParser().apply(ePair.getB());
                    Tuple2<Optional<G>, List<String>> gPair = xg.getParser().apply(fPair.getB());
                    return new Tuple2<>(aPair.getA().flatMap(a -> bPair.getA().flatMap(b -> cPair.getA().flatMap(c -> dPair.getA().flatMap(d -> ePair.getA().flatMap(e -> fPair.getA().flatMap(f -> gPair.getA().map(g -> new Tuple7<>(a, b, c, d, e, f, g)))))))), gPair.getB());
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters(), xd.getTabCompleters(), xe.getTabCompleters(), xf.getTabCompleters(), xf.getTabCompleters()))
        );
    }

    private static <A> List<A> flatten(Stream<List<A>> xs) {
        return xs
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
