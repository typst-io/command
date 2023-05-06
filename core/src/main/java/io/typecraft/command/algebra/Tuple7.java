// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typecraft.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple7<A, B, C, D, E, F, G> implements Tuple {
    A a;
    B b;
    C c;
    D d;
    E e;
    F f;
    G g;

    public <T> Tuple7<T, B, C, D, E, F, G> map1(Function<? super A, ? extends T> f) {
        return new Tuple7<>(f.apply(getA()), getB(), getC(), getD(), getE(), getF(), getG());
    }

    public <T> Tuple7<A, T, C, D, E, F, G> map2(Function<? super B, ? extends T> f) {
        return new Tuple7<>(getA(), f.apply(getB()), getC(), getD(), getE(), getF(), getG());
    }

    public <T> Tuple7<A, B, T, D, E, F, G> map3(Function<? super C, ? extends T> f) {
        return new Tuple7<>(getA(), getB(), f.apply(getC()), getD(), getE(), getF(), getG());
    }

    public <T> Tuple7<A, B, C, T, E, F, G> map4(Function<? super D, ? extends T> f) {
        return new Tuple7<>(getA(), getB(), getC(), f.apply(getD()), getE(), getF(), getG());
    }

    public <T> Tuple7<A, B, C, D, T, F, G> map5(Function<? super E, ? extends T> f) {
        return new Tuple7<>(getA(), getB(), getC(), getD(), f.apply(getE()), getF(), getG());
    }

    public <T> Tuple7<A, B, C, D, E, T, G> map6(Function<? super F, ? extends T> f) {
        return new Tuple7<>(getA(), getB(), getC(), getD(), getE(), f.apply(getF()), getG());
    }

    public <T> Tuple7<A, B, C, D, E, F, T> map7(Function<? super G, ? extends T> f) {
        return new Tuple7<>(getA(), getB(), getC(), getD(), getE(), getF(), f.apply(getG()));
    }

    @Override
    public int arity() {
        return 7;
    }
}
