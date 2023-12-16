package io.typecraft.command;

import lombok.Data;
import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value(staticConstructor = "of")
@With
public class CommandConfig {
    Function<CommandHelp, String> formatter;
    boolean hideNoPermissionCommands;
    public static final CommandConfig empty = new CommandConfig(CommandHelp::format, true);
}
