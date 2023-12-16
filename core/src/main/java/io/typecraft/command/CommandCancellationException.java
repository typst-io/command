package io.typecraft.command;

import java.util.concurrent.CancellationException;

/**
 * Empty exception for flow control
 */
public class CommandCancellationException extends CancellationException {
    public CommandCancellationException() {
    }

    public CommandCancellationException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        this.setStackTrace(new StackTraceElement[0]);
        return this;
    }
}
