package io.typecraft.command;

import lombok.Data;

import java.util.List;

public interface CommandTabResult<A> {
    static <A> CommandTabResult<A> suggestion(List<String> suggestions) {
        return new Suggestion<>(suggestions);
    }

    static <A> Present<A> present(String[] arguments, A a) {
        return new Present<>(arguments, a);
    }

    @Data
    class Suggestion<A> implements CommandTabResult<A> {
        private final List<String> suggestions;

        private Suggestion(List<String> suggestions) {
            this.suggestions = suggestions;
        }
    }

    @Data
    class Present<A> implements CommandTabResult<A> {
        private final String[] arguments;
        private final A command;

        private Present(String[] arguments, A command) {
            this.arguments = arguments;
            this.command = command;
        }
    }
}
