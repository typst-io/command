package io.typecraft.command;

import lombok.Data;
import lombok.With;

import java.util.function.Function;

@Data(staticConstructor = "of")
@With
public class CommandConfig {
    private final Function<CommandHelp, String> formatter;
    public static final CommandConfig empty = new CommandConfig(CommandHelp::format);
}
