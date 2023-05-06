// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typecraft.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple4<A, B, C, D> implements Tuple {
    A a;
    B b;
    C c;
    D d;

    public <T> Tuple4<T, B, C, D> map1(Function<? super A, ? extends T> f) {
        return new Tuple4<>(f.apply(getA()), getB(), getC(), getD());
    }

    public <T> Tuple4<A, T, C, D> map2(Function<? super B, ? extends T> f) {
        return new Tuple4<>(getA(), f.apply(getB()), getC(), getD());
    }

    public <T> Tuple4<A, B, T, D> map3(Function<? super C, ? extends T> f) {
        return new Tuple4<>(getA(), getB(), f.apply(getC()), getD());
    }

    public <T> Tuple4<A, B, C, T> map4(Function<? super D, ? extends T> f) {
        return new Tuple4<>(getA(), getB(), getC(), f.apply(getD()));
    }

    @Override
    public int arity() {
        return 4;
    }
}
