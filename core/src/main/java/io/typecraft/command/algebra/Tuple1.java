// This is an auto-generated code by the Gradle task 'generateTuples' in the project 'command-core'.
package io.typecraft.command.algebra;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value
@With
public class Tuple1<A> implements Tuple {
    A a;

    public <T> Tuple1<T> map1(Function<? super A, ? extends T> f) {
        return new Tuple1<>(f.apply(getA()));
    }

    @Override
    public int arity() {
        return 1;
    }
}
