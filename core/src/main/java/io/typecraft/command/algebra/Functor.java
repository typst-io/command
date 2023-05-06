package io.typecraft.command.algebra;

import java.util.function.Function;

public class Functor {
    public static <A, B> Option<B> map(Option<A> x, Function<? super A,  ? extends B> f) {
        if (x instanceof Option.Some) {
            A value = ((Option.Some<A>) x).getValue();
            return new Option.Some<>(f.apply(value));
        }
        return new Option.None<>();
    }
}
