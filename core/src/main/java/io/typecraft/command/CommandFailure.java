package io.typecraft.command;

import lombok.Data;

import java.util.List;

public interface CommandFailure<A> {
    @Data
    class FewArguments<A> implements CommandFailure<A> {
        private final String[] arguments;
        private final int index;
        private final Command<A> command;
    }

    @Data
    class UnknownSubCommand<A> implements CommandFailure<A> {
        private final String[] arguments;
        private final int index;
        private final Command<A> command;
    }

    @Data
    class ParsingFailure<A> implements CommandFailure<A> {
        private final List<String> names;
        private final Command<A> command;
    }
}
