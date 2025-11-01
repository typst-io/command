package io.typst.command;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value(staticConstructor = "of")
@With
public class CommandConfig {
    // TODO: data instead of function
    Function<CommandHelp, String> formatter;
    boolean hideNoPermissionCommands;
    public static final CommandConfig empty = new CommandConfig(CommandHelp::format, true);
}
