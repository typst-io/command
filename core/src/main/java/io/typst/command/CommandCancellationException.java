package io.typst.command;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;

/**
 * Exception for command flow control, optionally carrying failure details.
 *
 * <p>When a command argument parsing fails, this exception can carry the
 * {@link CommandFailure} information indicating which argument failed.</p>
 */
public class CommandCancellationException extends CancellationException {
    @Nullable
    private final CommandFailure<?> failure;

    public CommandCancellationException() {
        this.failure = null;
    }

    public CommandCancellationException(String message) {
        super(message);
        this.failure = null;
    }

    public CommandCancellationException(CommandFailure<?> failure) {
        super(formatMessage(failure));
        this.failure = failure;
    }

    public CommandCancellationException(String message, CommandFailure<?> failure) {
        super(message);
        this.failure = failure;
    }

    /**
     * Returns the failure details if available.
     *
     * @return the command failure, or null if not available
     */
    @Nullable
    public CommandFailure<?> getFailure() {
        return failure;
    }

    /**
     * Returns the index of the argument where parsing failed.
     *
     * @return the argument index, or -1 if not available
     */
    public int getFailedArgumentIndex() {
        if (failure instanceof CommandFailure.ParsingFailure) {
            return ((CommandFailure.ParsingFailure<?>) failure).getIndex();
        } else if (failure instanceof CommandFailure.FewArguments) {
            return ((CommandFailure.FewArguments<?>) failure).getIndex();
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            return ((CommandFailure.UnknownSubCommand<?>) failure).getIndex();
        }
        return -1;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        this.setStackTrace(new StackTraceElement[0]);
        return this;
    }

    private static String formatMessage(CommandFailure<?> failure) {
        if (failure instanceof CommandFailure.ParsingFailure) {
            CommandFailure.ParsingFailure<?> pf = (CommandFailure.ParsingFailure<?>) failure;
            return "Parsing failed at argument index " + pf.getIndex();
        } else if (failure instanceof CommandFailure.FewArguments) {
            CommandFailure.FewArguments<?> fa = (CommandFailure.FewArguments<?>) failure;
            return "Too few arguments at index " + fa.getIndex();
        } else if (failure instanceof CommandFailure.UnknownSubCommand) {
            CommandFailure.UnknownSubCommand<?> us = (CommandFailure.UnknownSubCommand<?>) failure;
            return "Unknown subcommand at index " + us.getIndex();
        }
        return "Command parsing failed";
    }
}
