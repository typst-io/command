// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typecraft.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple5<A, B, C, D, E> implements Tuple {
    A a;
    B b;
    C c;
    D d;
    E e;

    public <T> Tuple5<T, B, C, D, E> map1(Function<? super A, ? extends T> f) {
        return new Tuple5<>(f.apply(getA()), getB(), getC(), getD(), getE());
    }

    public <T> Tuple5<A, T, C, D, E> map2(Function<? super B, ? extends T> f) {
        return new Tuple5<>(getA(), f.apply(getB()), getC(), getD(), getE());
    }

    public <T> Tuple5<A, B, T, D, E> map3(Function<? super C, ? extends T> f) {
        return new Tuple5<>(getA(), getB(), f.apply(getC()), getD(), getE());
    }

    public <T> Tuple5<A, B, C, T, E> map4(Function<? super D, ? extends T> f) {
        return new Tuple5<>(getA(), getB(), getC(), f.apply(getD()), getE());
    }

    public <T> Tuple5<A, B, C, D, T> map5(Function<? super E, ? extends T> f) {
        return new Tuple5<>(getA(), getB(), getC(), getD(), f.apply(getE()));
    }

    @Override
    public int arity() {
        return 5;
    }
}
