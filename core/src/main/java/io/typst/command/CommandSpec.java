package io.typst.command;

import lombok.Data;
import lombok.With;

import java.util.Collections;
import java.util.List;

@Data(staticConstructor = "of")
@With
public class CommandSpec {
    private final List<String> arguments;
    private final String description;
    private final String permission;
    public static final CommandSpec empty = new CommandSpec(Collections.emptyList(), "", "");

    public static CommandSpec from(Command<?> node) {
        if (node instanceof Command.Parser) {
            Command.Parser<?> parser = (Command.Parser<?>) node;
            return CommandSpec.of(
                    parser.getNames(),
                    parser.getDescription(),
                    parser.getPermission()
            );
        }
        return CommandSpec.empty;
    }
}
