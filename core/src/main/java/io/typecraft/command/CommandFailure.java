package io.typecraft.command;

import lombok.Data;

import java.util.List;

public interface CommandFailure {
    @Data
    class FewArguments implements CommandFailure {
        private final String[] arguments;
        private final int index;
    }

    @Data
    class UnknownSubCommand implements CommandFailure {
        private final String[] arguments;
        private final int index;
    }

    @Data
    class ParsingFailure implements CommandFailure {
        private final List<String> names;
    }
}
