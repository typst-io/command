// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typst.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple2<A, B> implements Tuple {
    A a;
    B b;

    public <T> Tuple2<T, B> map1(Function<? super A, ? extends T> f) {
        return new Tuple2<>(f.apply(getA()), getB());
    }

    public <T> Tuple2<A, T> map2(Function<? super B, ? extends T> f) {
        return new Tuple2<>(getA(), f.apply(getB()));
    }

    @Override
    public int arity() {
        return 2;
    }
}
