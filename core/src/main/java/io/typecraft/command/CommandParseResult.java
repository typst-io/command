package io.typecraft.command;

import lombok.Data;

public interface CommandParseResult<A> {
    static <A> CommandParseResult<A> success(CommandSuccess<A> success) {
        return new CommandParseResult.Success<>(success);
    }

    static <A> CommandParseResult<A> failure(CommandFailure failure) {
        return new CommandParseResult.Failure<>(failure);
    }

    @Data
    class Success<A> implements CommandParseResult<A> {
        private final CommandSuccess<A> success;

        private Success(CommandSuccess<A> success) {
            this.success = success;
        }
    }

    @Data
    class Failure<A> implements CommandParseResult<A> {
        private final CommandFailure failure;

        private Failure(CommandFailure failure) {
            this.failure = failure;
        }
    }
}
