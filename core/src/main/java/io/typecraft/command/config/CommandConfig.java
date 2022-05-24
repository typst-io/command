package io.typecraft.command.config;

import io.typecraft.command.i18n.Language;
import io.typecraft.command.i18n.MessageId;
import io.typecraft.command.i18n.PluginLanguage;
import io.vavr.Tuple2;
import lombok.Data;
import lombok.With;

import java.util.*;
import java.util.stream.Collectors;

@Data(staticConstructor = "of")
@With
public class CommandConfig {
    private final Locale defaultLocale;
    private final Map<Locale, Map<String, String>> baseLangs;
    private final Map<String, Map<Locale, Map<String, String>>> pluginLangs;
    private static final CommandConfig defaultConfig =
            new CommandConfig(
                    Locale.getDefault(),
                    MessageId.defaultLangs,
                    Collections.emptyMap()
            );

    public static CommandConfig ofDefault() {
        return defaultConfig;
    }

    public Map<String, String> getPluginMessagesWithBase(Locale locale, String pluginName) {
        Map<Locale, Map<String, String>> langs = getPluginLangsWithBase(pluginName);
        Locale key = langs.containsKey(locale)
                ? locale
                : getSimilarLocale(locale, langs.keySet()).orElse(locale);
        return langs.getOrDefault(key, Collections.emptyMap());
    }

    private Map<Locale, Map<String, String>> getPluginLangsWithBase(String pluginName) {
        HashMap<Locale, Map<String, String>> langs = new HashMap<>(pluginLangs.getOrDefault(pluginName, Collections.emptyMap()));
        for (Map.Entry<Locale, Map<String, String>> pair : getBaseLangs().entrySet()) {
            Map<String, String> messages = new HashMap<>(langs.getOrDefault(pair.getKey(), Collections.emptyMap()));
            messages.putAll(pair.getValue());
            langs.put(pair.getKey(), messages);
        }
        return langs;
    }

    public static CommandConfig from(Map<String, Object> configMap, List<Language> baseLangs, List<PluginLanguage> pluginLangs) {
        Locale defaultLocale = Language.parseLocaleFrom(configMap.getOrDefault("default-locale", "").toString())
                .orElse(Locale.ENGLISH);
        Map<Locale, Map<String, String>> baseLangMap = new HashMap<>();
        Map<String, Map<Locale, Map<String, String>>> pluginLangMap = new HashMap<>();
        for (Language baseLang : baseLangs) {
            Map<String, String> messages = baseLangMap.computeIfAbsent(baseLang.getLocale(), k -> new HashMap<>());
            messages.putAll(baseLang.getMessages());
        }
        for (PluginLanguage pluginLang : pluginLangs) {
            Map<Locale, Map<String, String>> langs = pluginLangMap.computeIfAbsent(pluginLang.getPluginName(), k -> new HashMap<>());
            Map<String, String> messages = langs.computeIfAbsent(pluginLang.getLocale(), k -> new HashMap<>());
            messages.putAll(pluginLang.getMessages());
        }
        return CommandConfig.of(
                defaultLocale,
                makeReadonly(baseLangMap),
                makeReadonly(pluginLangMap)
        );
    }

    private static <A, B, C> Map<A, Map<B, C>> makeReadonly(Map<A, Map<B, C>> map) {
        // To avoid unexpect behaviors
        Map<A, Map<B, C>> newMap = map.entrySet().stream()
                .map(pair -> new Tuple2<>(pair.getKey(), Collections.unmodifiableMap(pair.getValue())))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
        return Collections.unmodifiableMap(newMap);
    }

    private static Optional<Locale> getSimilarLocale(Locale locale, Collection<Locale> locales) {
        return locales.stream()
                .filter(a -> a.getLanguage().equalsIgnoreCase(locale.getLanguage()))
                .findAny();
    }
}
