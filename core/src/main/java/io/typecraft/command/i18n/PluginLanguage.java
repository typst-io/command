package io.typecraft.command.i18n;

import lombok.Data;
import lombok.With;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Data
@With
public class PluginLanguage {
    private final String pluginName;
    private final Locale locale;
    private final Map<String, String> messages;

    public Language toLanguage() {
        return new Language(getLocale(), getMessages());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("plugin", getPluginName());
        map.putAll(toLanguage().toMap());
        return map;
    }

    public static Optional<PluginLanguage> from(Map<String, Object> map) {
        String pluginName = map.getOrDefault("plugin", "").toString();
        return !pluginName.isEmpty()
                ? Optional.of(fromLanguage(pluginName, Language.from(map)))
                : Optional.empty();
    }

    public static PluginLanguage fromLanguage(String pluginName, Language language) {
        return new PluginLanguage(pluginName, language.getLocale(), language.getMessages());
    }
}
