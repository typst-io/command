package io.typecraft.command;

import lombok.Data;
import lombok.With;

import java.util.*;

@With
@Data
public class LangId {
    private final String id;
    private final String message;
    public static final LangId commandNotExists = LangId.of("command-not-exists");
    public static final LangId commandWrongUsage = LangId.of("command-wrong-usage");
    public static final LangId typeString = LangId.of("string");
    public static final LangId typeInt = LangId.of("int");
    public static final LangId typeLong = LangId.of("long");
    public static final LangId typeFloat = LangId.of("float");
    public static final LangId typeDouble = LangId.of("double");
    public static final LangId typeBool = LangId.of("bool");
    public static final LangId typeStrings = LangId.of("strings");
    public static final Map<Locale, Map<String, String>> defaultLangs = getDefaultLangs();

    private LangId(String id, String message) {
        this.id = id;
        this.message = message;
    }

    public static LangId of(String id) {
        return new LangId(id, "");
    }

    public String getMessage(Map<String, String> messages) {
        return messages.getOrDefault(getId(), getMessage().isEmpty() ? getId() : getMessage());
    }

    public static Map<String, String> getDefaultMessages(Locale locale) {
        Locale key = defaultLangs.containsKey(locale) ? locale : Locale.ENGLISH;
        return defaultLangs.getOrDefault(key, Collections.emptyMap());
    }

    private static Map<Locale, Map<String, String>> getDefaultLangs() {
        Map<Locale, Map<String, String>> map = new HashMap<>();
        map.put(Locale.KOREAN, getKoreanMessages());
        map.put(Locale.ENGLISH, getEnglishMessages());
        return map;
    }

    private static Map<String, String> getKoreanMessages() {
        Map<String, String> map = new HashMap<>();
        // commands
        map.put(commandNotExists.getId(), "'%s' 명령어는 존재하지 않습니다!");
        map.put(commandWrongUsage.getId(), "잘못된 명령어입니다!");
        // types
        map.put(typeString.getId(), "이름");
        map.put(typeInt.getId(), "정수");
        map.put(typeLong.getId(), "정수");
        map.put(typeFloat.getId(), "실수");
        map.put(typeDouble.getId(), "실수");
        map.put(typeBool.getId(), "bool");
        return map;
    }

    private static Map<String, String> getEnglishMessages() {
        Map<String, String> map = new HashMap<>();
        // commands
        map.put(commandNotExists.getId(), "Command '%s' doesn't exists!");
        map.put(commandWrongUsage.getId(), "Wrong command!");
        // types
        map.put(typeString.getId(), "name");
        map.put(typeInt.getId(), "int");
        map.put(typeLong.getId(), "long");
        map.put(typeFloat.getId(), "float");
        map.put(typeDouble.getId(), "double");
        map.put(typeBool.getId(), "bool");
        return map;
    }
}
