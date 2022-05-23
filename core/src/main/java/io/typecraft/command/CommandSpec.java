package io.typecraft.command;

import lombok.Data;
import lombok.With;

import java.util.Collections;
import java.util.List;

@Data(staticConstructor = "of")
@With
public class CommandSpec {
    private final List<LangId> arguments;
    private final LangId descriptionId;
    public static final CommandSpec empty = new CommandSpec(Collections.emptyList(), LangId.of(""));
}
