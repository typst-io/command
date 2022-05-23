package io.typecraft.command.bukkit;

import io.typecraft.command.LangId;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class BukkitLangId {

    public static final LangId typeBukkitPlayer = LangId.of("player");
    public static final LangId typeBukkitOfflinePlayer = LangId.of("offline-player");
    public static final Map<Locale, Map<String, String>> defaultLangs = getDefaultLangs();

    private static Map<Locale, Map<String, String>> getDefaultLangs() {
        Map<Locale, Map<String, String>> map = new HashMap<>(LangId.defaultLangs);
        Map<String, String> englishes = new LinkedHashMap<>(map.getOrDefault(Locale.ENGLISH, Collections.emptyMap()));
        englishes.putAll(getEnglishMessages());
        map.put(Locale.ENGLISH, englishes);
        Map<String, String> koreans = new LinkedHashMap<>(map.getOrDefault(Locale.KOREAN, Collections.emptyMap()));
        koreans.putAll(getKoreanMessages());
        map.put(Locale.KOREAN, koreans);
        return map;
    }

    private static Map<String, String> getKoreanMessages() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(typeBukkitPlayer.getId(), "유저명");
        map.put(typeBukkitOfflinePlayer.getId(), "유저명");
        return map;
    }

    private static Map<String, String> getEnglishMessages() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(typeBukkitPlayer.getId(), "username");
        map.put(typeBukkitOfflinePlayer.getId(), "username");
        return map;
    }
}
