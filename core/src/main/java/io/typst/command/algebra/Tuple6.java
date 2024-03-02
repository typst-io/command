// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typst.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple6<A, B, C, D, E, F> implements Tuple {
    A a;
    B b;
    C c;
    D d;
    E e;
    F f;

    public <T> Tuple6<T, B, C, D, E, F> map1(Function<? super A, ? extends T> f) {
        return new Tuple6<>(f.apply(getA()), getB(), getC(), getD(), getE(), getF());
    }

    public <T> Tuple6<A, T, C, D, E, F> map2(Function<? super B, ? extends T> f) {
        return new Tuple6<>(getA(), f.apply(getB()), getC(), getD(), getE(), getF());
    }

    public <T> Tuple6<A, B, T, D, E, F> map3(Function<? super C, ? extends T> f) {
        return new Tuple6<>(getA(), getB(), f.apply(getC()), getD(), getE(), getF());
    }

    public <T> Tuple6<A, B, C, T, E, F> map4(Function<? super D, ? extends T> f) {
        return new Tuple6<>(getA(), getB(), getC(), f.apply(getD()), getE(), getF());
    }

    public <T> Tuple6<A, B, C, D, T, F> map5(Function<? super E, ? extends T> f) {
        return new Tuple6<>(getA(), getB(), getC(), getD(), f.apply(getE()), getF());
    }

    public <T> Tuple6<A, B, C, D, E, T> map6(Function<? super F, ? extends T> f) {
        return new Tuple6<>(getA(), getB(), getC(), getD(), getE(), f.apply(getF()));
    }

    @Override
    public int arity() {
        return 6;
    }
}
