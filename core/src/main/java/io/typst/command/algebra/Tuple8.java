// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typst.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple8<A, B, C, D, E, F, G, H> implements Tuple {
    A a;
    B b;
    C c;
    D d;
    E e;
    F f;
    G g;
    H h;

    public <T> Tuple8<T, B, C, D, E, F, G, H> map1(Function<? super A, ? extends T> f) {
        return new Tuple8<>(f.apply(getA()), getB(), getC(), getD(), getE(), getF(), getG(), getH());
    }

    public <T> Tuple8<A, T, C, D, E, F, G, H> map2(Function<? super B, ? extends T> f) {
        return new Tuple8<>(getA(), f.apply(getB()), getC(), getD(), getE(), getF(), getG(), getH());
    }

    public <T> Tuple8<A, B, T, D, E, F, G, H> map3(Function<? super C, ? extends T> f) {
        return new Tuple8<>(getA(), getB(), f.apply(getC()), getD(), getE(), getF(), getG(), getH());
    }

    public <T> Tuple8<A, B, C, T, E, F, G, H> map4(Function<? super D, ? extends T> f) {
        return new Tuple8<>(getA(), getB(), getC(), f.apply(getD()), getE(), getF(), getG(), getH());
    }

    public <T> Tuple8<A, B, C, D, T, F, G, H> map5(Function<? super E, ? extends T> f) {
        return new Tuple8<>(getA(), getB(), getC(), getD(), f.apply(getE()), getF(), getG(), getH());
    }

    public <T> Tuple8<A, B, C, D, E, T, G, H> map6(Function<? super F, ? extends T> f) {
        return new Tuple8<>(getA(), getB(), getC(), getD(), getE(), f.apply(getF()), getG(), getH());
    }

    public <T> Tuple8<A, B, C, D, E, F, T, H> map7(Function<? super G, ? extends T> f) {
        return new Tuple8<>(getA(), getB(), getC(), getD(), getE(), getF(), f.apply(getG()), getH());
    }

    public <T> Tuple8<A, B, C, D, E, F, G, T> map8(Function<? super H, ? extends T> f) {
        return new Tuple8<>(getA(), getB(), getC(), getD(), getE(), getF(), getG(), f.apply(getH()));
    }

    @Override
    public int arity() {
        return 8;
    }
}
