package io.typecraft.command.product;

import io.typecraft.command.Argument;
import io.vavr.*;

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
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.map(b -> new Tuple2<>(a, b))), bPair._2);
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters()))
        );
    }

    public static <A, B, C> Argument<Tuple3<A, B, C>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.flatMap(b -> cPair._1.map(c -> new Tuple3<>(a, b, c)))), cPair._2);
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters()))
        );
    }

    public static <A, B, C, D> Argument<Tuple4<A, B, C, D>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair._2);
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.flatMap(b -> cPair._1.flatMap(c -> dPair._1.map(d -> new Tuple4<>(a, b, c, d))))), dPair._2);
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters(), xd.getTabCompleters()))
        );
    }

    public static <A, B, C, D, E> Argument<Tuple5<A, B, C, D, E>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd, Argument<E> xe) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames(), xe.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair._2);
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair._2);
                    Tuple2<Optional<E>, List<String>> ePair = xe.getParser().apply(dPair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.flatMap(b -> cPair._1.flatMap(c -> dPair._1.flatMap(d -> ePair._1.map(e -> new Tuple5<>(a, b, c, d, e)))))), ePair._2);
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters(), xd.getTabCompleters(), xe.getTabCompleters()))
        );
    }

    public static <A, B, C, D, E, F> Argument<Tuple6<A, B, C, D, E, F>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd, Argument<E> xe, Argument<F> xf) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames(), xe.getNames(), xf.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair._2);
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair._2);
                    Tuple2<Optional<E>, List<String>> ePair = xe.getParser().apply(dPair._2);
                    Tuple2<Optional<F>, List<String>> fPair = xf.getParser().apply(ePair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.flatMap(b -> cPair._1.flatMap(c -> dPair._1.flatMap(d -> ePair._1.flatMap(e -> fPair._1.map(f -> new Tuple6<>(a, b, c, d, e, f))))))), fPair._2);
                },
                flatten(Stream.of(xa.getTabCompleters(), xb.getTabCompleters(), xc.getTabCompleters(), xd.getTabCompleters(), xe.getTabCompleters(), xf.getTabCompleters()))
        );
    }

    public static <A, B, C, D, E, F, G> Argument<Tuple7<A, B, C, D, E, F, G>> product(Argument<A> xa, Argument<B> xb, Argument<C> xc, Argument<D> xd, Argument<E> xe, Argument<F> xf, Argument<G> xg) {
        return Argument.of(
                flatten(Stream.of(xa.getNames(), xb.getNames(), xc.getNames(), xd.getNames(), xe.getNames(), xf.getNames(), xg.getNames())),
                args -> {
                    Tuple2<Optional<A>, List<String>> aPair = xa.getParser().apply(args);
                    Tuple2<Optional<B>, List<String>> bPair = xb.getParser().apply(aPair._2);
                    Tuple2<Optional<C>, List<String>> cPair = xc.getParser().apply(bPair._2);
                    Tuple2<Optional<D>, List<String>> dPair = xd.getParser().apply(cPair._2);
                    Tuple2<Optional<E>, List<String>> ePair = xe.getParser().apply(dPair._2);
                    Tuple2<Optional<F>, List<String>> fPair = xf.getParser().apply(ePair._2);
                    Tuple2<Optional<G>, List<String>> gPair = xg.getParser().apply(fPair._2);
                    return new Tuple2<>(aPair._1.flatMap(a -> bPair._1.flatMap(b -> cPair._1.flatMap(c -> dPair._1.flatMap(d -> ePair._1.flatMap(e -> fPair._1.flatMap(f -> gPair._1.map(g -> new Tuple7<>(a, b, c, d, e, f, g)))))))), gPair._2);
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
