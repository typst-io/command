package io.typst.command.bukkit;

import io.typst.command.CommandConfig;
import io.typst.command.LangKey;
import io.typst.command.MessageKey;
import lombok.Value;
import lombok.With;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Value(staticConstructor = "of")
@With
public class BukkitCommandConfig {
    Function<BukkitCommandHelp, String> formatter;
    boolean hideNoPermissionCommands;
    Map<LangKey, Map<MessageKey, String>> messages;
    public static final BukkitCommandConfig empty = new BukkitCommandConfig(BukkitCommandHelp::format, true, LangKey.getLanguageMap());

    public static BukkitCommandConfig from(CommandConfig config) {
        return new BukkitCommandConfig(help -> config.getFormatter().apply(help.toHelp()), config.isHideNoPermissionCommands(), LangKey.getLanguageMap());
    }

    public String formatMessage(LangKey langKey, MessageKey messageKey, Object... args) {
        return String.format(messages.getOrDefault(langKey, Collections.emptyMap()).getOrDefault(messageKey, messageKey.getDefaultMessage()), args);
    }

    public BukkitCommandConfig withMessage(LangKey langKey, MessageKey msgKey, String msg) {
        Map<LangKey, Map<MessageKey, String>> map = new LinkedHashMap<>(getMessages());
        Map<MessageKey, String> messages = new LinkedHashMap<>(map.getOrDefault(langKey, Collections.emptyMap()));
        messages.put(msgKey, msg);
        map.put(langKey, messages);
        return withMessages(map);
    }
}
