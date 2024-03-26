package io.typst.command;

import io.typst.command.algebra.Tuple2;
import lombok.Data;

import java.util.List;
import java.util.Optional;

public interface CommandTabResult<A> {
    @Data
    class Suggestions<A> implements CommandTabResult<A> {
        /**
         * Primal form was List&lt;String> now List&lt;(String, correspondNode)&gt;
         */
        // TODO: too complex
        private final List<Tuple2<String, Optional<Command<A>>>> suggestions;
    }

    @Data
    class Present<A> implements CommandTabResult<A> {
        private final String[] arguments;
        private final A command;
    }
}
