package io.typst.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum LangKey {
    ENGLISH("en"),
    KOREAN("ko"),
    ;

    private final String key;

    public static String getKoreanMessage(MessageKey key) {
        switch (key) {
            case UNKNOWN_SUB_COMMAND:
                return "'%s' 명령어는 존재하지 않습니다!";
            case INVALID_COMMAND:
                return "잘못된 명령어입니다!";
            case NO_PERMISSION:
                return "권한이 없습니다! `%s`";
            case INGAME_PLAYER_ONLY:
                return "게임에 접속해서 사용해주세요!";
            case NO_ITEM_IN_MAIN_HAND:
                return "손에 아이템을 들어주세요!";
            default:
                throw new IllegalArgumentException("Unknown MessageKey: " + key);
        }
    }

    public static Map<LangKey, Map<MessageKey, String>> getLanguageMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(Function.identity(), LangKey::toMessageMap));
    }

    public static LangKey getLanguageKeyFrom(String xs) {
        return Arrays.stream(values())
                .filter(lang -> lang.key.equalsIgnoreCase(xs))
                .findFirst()
                .orElse(ENGLISH);
    }

    public Map<MessageKey, String> toMessageMap() {
        switch (this) {
            case ENGLISH:
                return Arrays.stream(MessageKey.values())
                        .collect(Collectors.toMap(Function.identity(), MessageKey::getDefaultMessage));
            case KOREAN:
                return Arrays.stream(MessageKey.values())
                        .collect(Collectors.toMap(Function.identity(), LangKey::getKoreanMessage));
            default:
                throw new IllegalStateException("Unknown LangKey: " + this);
        }
    }

    public LangKey getJVMDefaultLanguage() {
        return Locale.getDefault().getLanguage().equals("ko_kr") ? KOREAN : ENGLISH;
    }
}
