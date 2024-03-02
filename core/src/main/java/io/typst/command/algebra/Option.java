package io.typst.command.algebra;

import lombok.Value;

import java.util.Optional;

public interface Option<A> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <A> Option<A> from(Optional<A> javaOptional) {
        A a = javaOptional.orElse(null);
        return a != null ? new Some<>(a) : new None<>();
    }

    default  Optional<A> asJavaOptional() {
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

    @Value
    class Some<A> implements Option<A> {
        A value;
    }

    class None<A> implements Option<A> {
    }
}
