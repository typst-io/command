package io.typecraft.command.i18n;

import io.typecraft.command.Converters;
import io.vavr.Tuple2;
import lombok.Data;
import lombok.With;

import java.util.*;
import java.util.stream.Stream;

@Data
@With
public class Language {
    private final Locale locale;
    private final Map<String, String> messages;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("locale", getLocale().toString());
        map.put("messages", getMessages());
        return map;
    }

    public static Language from(Map<String, Object> map) {
        Locale locale = Language.parseLocaleFrom(map.getOrDefault("locale", "").toString())
                .orElse(Locale.ENGLISH);
        Map<String, String> messages = Converters.toMapAs(
                pair -> new Tuple2<>(
                        pair._1.toString(),
                        pair._2.toString()
                ),
                map.get("messages")
        ).orElse(Collections.emptyMap());
        return new Language(
                locale,
                messages
        );
    }

    public static Optional<Locale> parseLocaleFrom(String localeFormat) {
        if (!localeFormat.isEmpty()) {
            String[] localePieces = localeFormat.split("_");
            return Optional.of(new Locale(
                    localePieces[0],
                    localePieces.length >= 2 ? localePieces[1] : ""
            ));
        } else {
            return Optional.empty();
        }
    }
}
