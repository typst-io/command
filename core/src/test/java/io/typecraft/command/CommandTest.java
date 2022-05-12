package io.typecraft.command;


import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static io.typecraft.command.Argument.intArg;
import static io.typecraft.command.Argument.strArg;
import static io.typecraft.command.Command.pair;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandTest {
    // MyCommand = AddItem | RemoveItem | ...
    private static final Command<MyCommand> rootCommand =
            Command.map(
                    pair("item", Command.map(
                            pair("open", Command.present(new OpenItemList())),
                            // intArg: Argument<Integer>
                            // strArg: Argument<String>
                            pair("add", Command.argument(AddItem::new, intArg, strArg)),
                            pair("remove", Command.argument(RemoveItem::new, intArg))
                    )),
                    pair("reload", Command.present(new ReloadCommand()))
            );

    public interface MyCommand {
    }

    public static class AddItem implements MyCommand {
        private final int index;
        private final String name;

        public AddItem(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @Override
        public String toString() {
            return "AddItem{" +
                    "index=" + index +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AddItem addItem = (AddItem) o;
            return index == addItem.index && Objects.equals(name, addItem.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, name);
        }
    }

    public static class RemoveItem implements MyCommand {
        public final int index;

        public RemoveItem(int index) {
            this.index = index;
        }
    }

    public static class OpenItemList implements MyCommand {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof OpenItemList;
        }
    }

    public static class ReloadCommand implements MyCommand {
    }

    // executor
    @Test
    public void unit() {
        String[] args = new String[0];
        assertEquals(
                Either.left(new CommandFailure.FewArguments(args, 0)),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void sub() {
        String[] args = new String[]{"item"};
        assertEquals(
                Either.left(new CommandFailure.FewArguments(args, 1)),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void subUnknown() {
        String[] args = new String[]{"item", "unknownCommand"};
        assertEquals(
                Either.left(new CommandFailure.UnknownSubCommand(args, 1)),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void present() {
        String[] args = new String[]{"item", "open"};
        assertEquals(
                Either.right(new CommandSuccess<>(args, 2, new OpenItemList())),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void argument() {
        int index = 0;
        String name = "someName";
        String[] args = new String[]{"item", "add", String.valueOf(index), name};
        assertEquals(
                Either.right(new CommandSuccess<>(new String[0], args.length, new AddItem(index, name))),
                Command.parse(args, rootCommand)
        );
    }

    // tab
    @Test
    public void tabUnit() {
        String[] args = new String[0];
        assertEquals(
                CommandTabResult.suggestion(Arrays.asList("item", "reload")),
                Command.tabComplete(args, rootCommand)
        );
    }

    @Test
    public void tabSub() {
        String[] args = new String[]{"item", ""};
        assertEquals(
                CommandTabResult.suggestion(Arrays.asList("open", "add", "remove")),
                Command.tabComplete(args, rootCommand)
        );
    }

    @Test
    public void tabSubUnknown() {
        String[] args = new String[]{"item", "unknownCommand"};
        assertEquals(
                CommandTabResult.suggestion(Collections.emptyList()),
                Command.tabComplete(args, rootCommand)
        );
    }
}
