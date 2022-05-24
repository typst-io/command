package io.typecraft.command;

import io.typecraft.command.i18n.MessageId;
import lombok.Data;
import lombok.With;

import java.util.Collections;
import java.util.List;

@Data(staticConstructor = "of")
@With
public class CommandSpec {
    private final List<MessageId> arguments;
    private final MessageId descriptionId;
    public static final CommandSpec empty = new CommandSpec(Collections.emptyList(), MessageId.of(""));
}
