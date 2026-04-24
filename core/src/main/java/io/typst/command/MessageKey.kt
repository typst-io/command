package io.typst.command

/**
 * See also: [LangKey]
 */
enum class MessageKey(val defaultMessage: String) {
    UNKNOWN_SUB_COMMAND("Command '%s' doesn't exists!"),
    INVALID_COMMAND("Wrong command!"),
    NO_PERMISSION("No permission! `%s`"),
    INGAME_PLAYER_ONLY("Ingame player only!"),
    NO_ITEM_IN_MAIN_HAND("Please hold an item on your main hand!"),
    ;
}