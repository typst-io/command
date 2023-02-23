package io.typecraft.command;

import io.vavr.*;
import io.vavr.control.Either;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.typecraft.command.StandardArguments.intArg;
import static io.vavr.API.Tuple;

public class CommandArgumentTest {
    private <A extends Tuple> void assertNode(String[] args, A a, Command.Parser<A> node) {
        int arity = a.arity();
        Assertions.assertEquals(arity, args.length);
        Assertions.assertEquals(arity, node.getNames().size());
        Assertions.assertEquals(arity, node.getTabCompleters().size());
        Assertions.assertEquals(
                Either.right(new CommandSuccess<>(args, arity, a)),
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
                Tuple(1),
                Command.argument(API::Tuple, intArg)
        );
    }

    @Test
    public void arg2() {
        assertNode(
                new String[]{"1", "2"},
                Tuple(1, 2),
                Command.argument(API::Tuple, intArg, intArg)
        );
    }

    @Test
    public void arg3() {
        assertNode(
                new String[]{"1", "2", "3"},
                Tuple(1, 2, 3),
                Command.argument(API::Tuple, intArg, intArg, intArg)
        );
    }

    @Test
    public void arg4() {
        assertNode(
                new String[]{"1", "2", "3", "4"},
                Tuple(1, 2, 3, 4),
                Command.argument(API::Tuple, intArg, intArg, intArg, intArg)
        );
    }

    @Test
    public void arg5() {
        assertNode(
                new String[]{"1", "2", "3", "4", "5"},
                Tuple(1, 2, 3, 4, 5),
                Command.argument(API::Tuple, intArg, intArg, intArg, intArg, intArg)
        );
    }

    @Test
    public void arg6() {
        assertNode(
                new String[]{"1", "2", "3", "4", "5", "6"},
                Tuple(1, 2, 3, 4, 5, 6),
                Command.argument(API::Tuple, intArg, intArg, intArg, intArg, intArg, intArg)
        );
    }

    @Test
    public void arg7() {
        assertNode(
                new String[]{"1", "2", "3", "4", "5", "6", "7"},
                Tuple(1, 2, 3, 4, 5, 6, 7),
                Command.argument(API::Tuple, intArg, intArg, intArg, intArg, intArg, intArg, intArg)
        );
    }
}
