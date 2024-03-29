package io.typst.command;

import io.typst.command.algebra.Either;
import io.typst.command.algebra.Tuple2;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Converters, following naming conventions:
 * <ul>
 *     <li>parse*: string parsing</li>
 *     <li>as*: type casting</li>
 *     <li>to*: value computation</li>
 * </ul>
 */
@UtilityClass
@ExtensionMethod({Either.class})
public class Converters {
    public static Optional<Integer> parseInt(String s) {
        return Either.catching(() -> Integer.parseInt(s)).toJavaOptional();
    }

    public static Optional<Long> parseLong(String s) {
        return Either.catching(() -> Long.parseLong(s)).toJavaOptional();
    }

    public static Optional<Float> parseFloat(String s) {
        return Either.catching(() -> Float.parseFloat(s)).toJavaOptional();
    }

    public static Optional<Double> parseDouble(String s) {
        return Either.catching(() -> Double.parseDouble(s)).toJavaOptional();
    }

    public static Optional<Boolean> parseBoolean(String s) {
        return Either.catching(() -> Boolean.parseBoolean(s)).toJavaOptional();
    }

    @SuppressWarnings("unchecked") // covariant
    public static Optional<Map<Object, Object>> asMap(Object o) {
        return o instanceof Map
                ? Optional.of((Map<Object, Object>) o)
                : Optional.empty();
    }

    public static <K, V> Optional<Map<K, V>> toMapAs(Function<Tuple2<Object, Object>, Tuple2<K, V>> f, Object o) {
        return asMap(o).map(m -> m.entrySet().stream()
                .map(pair -> f.apply(new Tuple2<>(pair.getKey(), pair.getValue())))
                .collect(Collectors.toMap(Tuple2::getA, Tuple2::getB, (a, b) -> b, LinkedHashMap::new)));
    }

    @SuppressWarnings("unchecked") // covariant
    public static Optional<Collection<Object>> asCollection(Object o) {
        return o instanceof Collection
                ? Optional.of((Collection<Object>) o)
                : Optional.empty();
    }

    public static <A> Optional<Collection<A>> toCollectionAs(Function<Object, A> f, Object o) {
        return asCollection(o).map(xs -> xs.stream()
                .map(f)
                .collect(Collectors.toList()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <A> Stream<A> toStream(Optional<A> aO) {
        A a = aO.orElse(null);
        return a != null ? Stream.of(a) : Stream.empty();
    }

    public static <A, B> Function<A, Stream<B>> toStreamF(Function<A, Optional<B>> f) {
        return a -> toStream(f.apply(a));
    }
}
