// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typst.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple3<A, B, C> implements Tuple {
    A a;
    B b;
    C c;

    public <T> Tuple3<T, B, C> map1(Function<? super A, ? extends T> f) {
        return new Tuple3<>(f.apply(getA()), getB(), getC());
    }

    public <T> Tuple3<A, T, C> map2(Function<? super B, ? extends T> f) {
        return new Tuple3<>(getA(), f.apply(getB()), getC());
    }

    public <T> Tuple3<A, B, T> map3(Function<? super C, ? extends T> f) {
        return new Tuple3<>(getA(), getB(), f.apply(getC()));
    }

    @Override
    public int arity() {
        return 3;
    }
}
