package io.typst.command.algebra;

import lombok.Value;

import java.util.Optional;
import java.util.function.Function;

public interface Option<A> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <A> Option<A> from(Optional<A> javaOptional) {
        A a = javaOptional.orElse(null);
        return a != null ? new Some<>(a) : new None<>();
    }

    default Optional<A> asJavaOptional() {
        return this instanceof Some
                ? Optional.of(((Some<A>) this).getValue())
                : Optional.empty();
    }

    default boolean isDefined() {
        return this instanceof Some;
    }

    default A getOrNull() {
        return this instanceof Some ? ((Some<A>) this).getValue() : null;
    }

    default <B> Option<B> map(Function<? super A, ? extends B> f) {
        return Functor.map(this, f);
    }

    @Value
    class Some<A> implements Option<A> {
        A value;
    }

    class None<A> implements Option<A> {
    }
}
