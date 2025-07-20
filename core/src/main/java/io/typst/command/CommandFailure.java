package io.typst.command;

import lombok.Data;

import java.util.List;

public interface CommandFailure<A> {
    /**
     * This represents the node {@link Command.Mapping} declared to require more arguments but the fewer input arguments was accepted.
     * @param <A> the command of their business logic
     */
    @Data
    class FewArguments<A> implements CommandFailure<A> {
        private final String[] arguments;
        private final int index;
        private final Command<A> command;
    }

    /**
     * This represents the node was {@link Command.Mapping} but the sub command corresponding the input arguments wasn't exist.
     * @param <A> the command of their business logic
     */
    @Data
    class UnknownSubCommand<A> implements CommandFailure<A> {
        private final String[] arguments;
        private final int index;
        private final Command<A> command;
    }

    /**
     * This represents the node was {@link Command.Parser} but the input arguments couldn't be parsed.
     * @param <A> the command of their business logic
     */
    @Data
    class ParsingFailure<A> implements CommandFailure<A> {
        private final String[] arguments;
        private final int index;
        /**
         * The command node, owner of arguments.
         */
        private final Command<A> command;
        /**
         * The required arguments that couldn't parse.
         */
        private final List<Argument<?>> args;
    }
}
