package io.typst.command;

import lombok.Data;

@Data
public class CommandSuccess<A> {
    private final String[] arguments;
    private final int index;
    private final A command;
    private final Command<A> node;
}
