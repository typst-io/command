package io.typst.command;

import io.typst.command.algebra.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandArgumentTest {
    private <A extends Tuple> void assertNode(String[] args, A a, Command.Parser<A> node) {
        int arity = a.arity();
        Assertions.assertEquals(arity, args.length);
        Assertions.assertEquals(arity, node.getArguments().size());
        Assertions.assertEquals(
                new Either.Right<>(new CommandSuccess<>(args, arity, a, node)),
                Command.parse(
                        args,
                        node
                )
        );
    }


    @Test
    public void arg1() {
        assertNode(
                new String[]{"1"},
                new Tuple1<>(1),
                Command.argument(Tuple1::new, StandardArguments.intArg)
        );
    }

    @Test
    public void arg2() {
        assertNode(
                new String[]{"1", "2"},
                new Tuple2<>(1, 2),
                Command.argument(Tuple2::new, StandardArguments.intArg, StandardArguments.intArg)
        );
    }

    @Test
    public void arg3() {
        assertNode(
                new String[]{"1", "2", "3"},
                new Tuple3<>(1, 2, 3),
                Command.argument(Tuple3::new, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg)
        );
    }

    @Test
    public void arg4() {
        assertNode(
                new String[]{"1", "2", "3", "4"},
                new Tuple4<>(1, 2, 3, 4),
                Command.argument(Tuple4::new, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg)
        );
    }

    @Test
    public void arg5() {
        assertNode(
                new String[]{"1", "2", "3", "4", "5"},
                new Tuple5<>(1, 2, 3, 4, 5),
                Command.argument(Tuple5::new, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg)
        );
    }

    @Test
    public void arg6() {
        assertNode(
                new String[]{"1", "2", "3", "4", "5", "6"},
                new Tuple6<>(1, 2, 3, 4, 5, 6),
                Command.argument(Tuple6::new, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg)
        );
    }

    @Test
    public void arg7() {
        assertNode(
                new String[]{"1", "2", "3", "4", "5", "6", "7"},
                new Tuple7<>(1, 2, 3, 4, 5, 6, 7),
                Command.argument(Tuple7::new, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg, StandardArguments.intArg)
        );
    }
}
