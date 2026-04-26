package io.typst.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * See also: {@link LangKey}
 */
@Getter
@AllArgsConstructor
public enum MessageKey {
    UNKNOWN_SUB_COMMAND("Command '%s' doesn't exists!"),
    INVALID_COMMAND("Wrong command!"),
    NO_PERMISSION("No permission! `%s`"),
    INGAME_PLAYER_ONLY("Ingame player only!"),
    NO_ITEM_IN_MAIN_HAND("Please hold an item on your main hand!"),
    ;

    private final String defaultMessage;
}
