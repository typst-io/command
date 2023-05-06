package io.typecraft.command.algebra;

import lombok.Value;

import java.util.Optional;
import java.util.function.Supplier;

public interface Either<L, R> {

    default Optional<R> toJavaOptional() {
        return this instanceof Right
                ? Optional.of(((Right<L, R>) this).getRight())
                : Optional.empty();
    }

    static <A> Either<Throwable, A> catching(Supplier<A> f) {
        try {
            return new Right<>(f.get());
        } catch (Throwable ex) {
            return new Left<>(ex);
        }
    }

    @Value
    class Left<L, R> implements Either<L, R> {
        L left;
    }

    @Value
    class Right<L, R> implements Either<L, R> {
        R right;
    }
}
