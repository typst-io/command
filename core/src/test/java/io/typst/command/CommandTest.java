package io.typst.command;


import io.typst.command.algebra.Either;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.typst.command.Command.pair;
import static io.typst.command.StandardArguments.intArg;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandTest {
    private static final Argument<Integer> intTabArg = intArg.withTabCompletes(() -> Arrays.asList("10", "20"));
    private static final Argument<Integer> intTabArg2 = intArg.withTabCompletes(() -> Arrays.asList("30", "40"));
    // MyCommand = AddItem | RemoveItem | ...
    private static final Command.Mapping<MyCommand> itemCommand;
    private static final Command.Parser<MyCommand> pageCmd = Command.argument(PageItem::new, intTabArg, intTabArg2);

    static {
        itemCommand = Command.mapping(
                pair("open", Command.present(new OpenItemList()).withDescription("아이템 목록을 엽니다.")),
                // intArg: Argument<Integer>
                // strArg: Argument<String>
                pair("add", Command.argument(AddItem::new, intArg, StandardArguments.strArg)),
                pair("remove", Command.argument(RemoveItem::new, intArg)),
                pair("page", pageCmd),
                pair("lazy", Command.present(new AddItem(0, null))),
                pair("camelPage", pageCmd)
        );
    }

    private static final Command.Mapping<MyCommand> itemCommandWithFallback =
            itemCommand.withFallback(Command.present(new FallbackItem()));
    public static final Command.Mapping<MyCommand> itemCommandWithFallback2 =
            itemCommandWithFallback.withFallback(Command.argument(FallbackArg::new, intArg));
    public static final Command.Mapping<MyCommand> itemCommandWithFallback3 =
            itemCommandWithFallback.withFallback(Command.argument(FallbackOptArg::new, intArg.asOptional()));
    private static final Command<MyCommand> reloadCommand =
            Command.<MyCommand>present(new ReloadCommand()).withDescription("리로드합니다.");
    private static final Command<MyCommand> rootCommand =
            Command.mapping(
                    pair("item", itemCommand),
                    pair("reload", reloadCommand)
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
        public final Number index;

        public RemoveItem(Number index) {
            this.index = index;
        }
    }

    public static class PageItem implements MyCommand {
        public final int index;
        public final int indexB;

        public PageItem(int index, int indexB) {
            this.index = index;
            this.indexB = indexB;
        }
    }

    public static class OpenItemList implements MyCommand {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof OpenItemList;
        }
    }

    public static class FallbackItem implements MyCommand {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof FallbackItem;
        }
    }

    public static class FallbackArg implements MyCommand {
        public final int value;

        public FallbackArg(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FallbackArg;
        }
    }

    public static class FallbackOptArg implements MyCommand {
        public final Integer value;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public FallbackOptArg(Optional<Integer> value) {
            this.value = value.orElse(null);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FallbackOptArg;
        }
    }

    public static class ReloadCommand implements MyCommand {
    }

    // executor
    @Test
    public void unit() {
        String[] args = new String[0];
        assertEquals(
                new Either.Left<>(new CommandFailure.FewArguments<>(args, 0, rootCommand)),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void sub() {
        String[] args = new String[]{"item"};
        assertEquals(
                new Either.Left<>(new CommandFailure.FewArguments<>(args, 1, itemCommand)),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void subUnknown() {
        String[] args = new String[]{"item", "unknownCommand"};
        assertEquals(
                new Either.Left<>(new CommandFailure.UnknownSubCommand<>(args, 1, itemCommand)),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void present() {
        String[] args = new String[]{"item", "open"};
        assertEquals(
                new Either.Right<>(new CommandSuccess<>(args, 2, new OpenItemList(), itemCommand.getCommandMap().get("open"))),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void argument() {
        int index = 0;
        String name = "someName";
        String[] args = new String[]{"item", "add", String.valueOf(index), name};
        assertEquals(
                new Either.Right<>(new CommandSuccess<>(args, args.length, new AddItem(index, name), itemCommand.getCommandMap().get("add"))),
                Command.parse(args, rootCommand)
        );
    }

    @Test
    public void help() {
        String[] args = new String[]{"item", "a"};
        Either<CommandFailure<MyCommand>, CommandSuccess<MyCommand>> result = Command.parse(args, rootCommand);
        CommandFailure<MyCommand> failure = result instanceof Either.Left
                ? ((Either.Left<CommandFailure<MyCommand>, CommandSuccess<MyCommand>>) result).getLeft()
                : null;
        if (failure instanceof CommandFailure.FewArguments) {
            CommandFailure.FewArguments<MyCommand> fewArgs = (CommandFailure.FewArguments<MyCommand>) failure;
            helpCommand(fewArgs.getArguments(), fewArgs.getIndex(), fewArgs.getCommand());
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand<MyCommand> unknown = (CommandFailure.UnknownSubCommand<MyCommand>) failure;
            helpCommand(unknown.getArguments(), unknown.getIndex(), unknown.getCommand());
        }
    }

    private <A> void helpCommand(String[] args, int position, Command<A> cmd) {
        String[] succArgs = args.length >= 1
                ? Arrays.copyOfRange(args, 0, position)
                : new String[0];
        for (Map.Entry<List<String>, Command<A>> pair : Command.getEntries(cmd)) {
            List<String> usageArgs = pair.getKey().stream()
                    .flatMap(s -> Stream.concat(
                            Arrays.stream(succArgs),
                            Stream.of(s)
                    ))
                    .collect(Collectors.toList());
            String description = CommandSpec.from(pair.getValue()).getDescription();
            String suffix = description.isEmpty()
                    ? ""
                    : " - " + description;
            System.out.println("/" + String.join(" ", usageArgs) + suffix);
        }
    }

    @Test
    public void fallback() {
        String[] args = new String[]{"item"};
        Command.Mapping<MyCommand> commandMap = Command.mapping(
                pair("item", itemCommandWithFallback)
        );
        Either<CommandFailure<MyCommand>, CommandSuccess<MyCommand>> result = Command.parse(args, commandMap);
        assertEquals(
                new Either.Right<>(new CommandSuccess<>(args, 1, new FallbackItem(), itemCommandWithFallback.getFallback().orElse(null))),
                result
        );
    }

    @Test
    public void fallbackNone() {
        String[] args2 = new String[]{"item", "help"};
        Command.Mapping<MyCommand> commandMap = Command.mapping(
                pair("item", itemCommandWithFallback)
        );
        Either<CommandFailure<MyCommand>, CommandSuccess<MyCommand>> result2 = Command.parse(args2, commandMap);
        assertEquals(
                new Either.Left<>(new CommandFailure.UnknownSubCommand<>(args2, 1, itemCommandWithFallback)),
                result2
        );
    }

    @Test
    public void fallbackArg() {
        String[] args = new String[]{"item", "1"};
        Command.Mapping<MyCommand> commandMap = Command.mapping(
                pair("item", itemCommandWithFallback2)
        );
        Either<CommandFailure<MyCommand>, CommandSuccess<MyCommand>> result3 = Command.parse(args, commandMap);
        assertEquals(
                new Either.Right<>(new CommandSuccess<>(args, 2, new FallbackArg(1), itemCommandWithFallback2.getFallback().orElse(null))),
                result3
        );
    }

    @Test
    public void fallbackOptionalArg() {
        String[] args = new String[]{"item", "help"};
        Command.Mapping<MyCommand> commandMap = Command.mapping(
                pair("item", itemCommandWithFallback3)
        );
        Either<CommandFailure<MyCommand>, CommandSuccess<MyCommand>> result3 = Command.parse(args, commandMap);
        assertEquals(
                new Either.Left<>(new CommandFailure.UnknownSubCommand<>(args, 1, itemCommandWithFallback3)),
                result3
        );
    }

    // tab
    @Test
    public void tabUnit() {
        String[] args = new String[0];
        assertEquals(
                new CommandTabResult.Suggestions<>(Arrays.asList(
                        pair("item", Optional.of(itemCommand)),
                        pair("reload", Optional.of(reloadCommand))
                )),
                Command.tabComplete(args, rootCommand)
        );
    }

    @Test
    public void tabSub() {
        String[] args = new String[]{"item", ""};
        assertEquals(
                new CommandTabResult.Suggestions<>(Stream.of(
                        "open", "add", "remove", "page", "lazy", "camelPage"
                ).map(it ->
                        pair(it, Optional.ofNullable(itemCommand.getCommandMap().get(it)))
                ).collect(Collectors.toList())),
                Command.tabComplete(args, rootCommand)
        );
    }

    @Test
    public void tabSubUnknown() {
        String[] args = new String[]{"item", "unknownCommand"};
        assertEquals(
                new CommandTabResult.Suggestions<>(Collections.emptyList()),
                Command.tabComplete(args, rootCommand)
        );
    }

    @Test
    public void tabCustom() {
        assertEquals(
                new CommandTabResult.Suggestions<>(Stream.of("10", "20").map(
                        it -> pair(it, Optional.<Command<MyCommand>>of(pageCmd))
                ).collect(Collectors.toList())),
                Command.tabComplete(new String[]{"item", "page", ""}, rootCommand)
        );
        assertEquals(
                new CommandTabResult.Suggestions<>(Stream.of("10").map(
                        it -> pair(it, Optional.<Command<MyCommand>>of(pageCmd))
                ).collect(Collectors.toList())),
                Command.tabComplete(new String[]{"item", "page", "1"}, rootCommand)
        );
    }

    @Test
    public void tabCustom2() {
        assertEquals(
                new CommandTabResult.Suggestions<>(Stream.of("30", "40").map(
                        it -> pair(it, Optional.<Command<MyCommand>>of(pageCmd))
                ).collect(Collectors.toList())),
                Command.tabComplete(new String[]{"item", "page", "10", ""}, rootCommand)
        );
        assertEquals(
                new CommandTabResult.Suggestions<>(Stream.of("30").map(
                        it -> pair(it, Optional.<Command<MyCommand>>of(pageCmd))
                ).collect(Collectors.toList())),
                Command.tabComplete(new String[]{"item", "page", "10", "3"}, rootCommand)
        );
    }

    @Test
    public void tabUpperCase() {
        assertEquals(
                new CommandTabResult.Suggestions<>(Stream.of("30", "40").map(
                        it -> pair(it, Optional.<Command<MyCommand>>of(pageCmd))
                ).collect(Collectors.toList())),
                Command.tabComplete(new String[]{"item", "camelPage", "10", ""}, rootCommand)
        );
    }
}
