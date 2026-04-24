package io.typst.command

import io.typst.command.MessageKey.*
import java.util.*

enum class LangKey(val key: String) {
    ENGLISH("en"),
    KOREAN("ko"),
    ;

    companion object {
        @JvmStatic
        fun getKoreanMessage(key: MessageKey): String =
            when (key) {
                UNKNOWN_SUB_COMMAND -> "'%s' 명령어는 존재하지 않습니다!"
                INVALID_COMMAND -> "잘못된 명령어입니다!"
                NO_PERMISSION -> "§cNo permission! `%s`"
                INGAME_PLAYER_ONLY -> "게임에 접속해서 사용해주세요!"
                NO_ITEM_IN_MAIN_HAND -> "손에 아이템을 들어주세요!"
            }

        @JvmStatic
        fun getLanguageMap(): Map<LangKey, Map<MessageKey, String>> =
            LangKey.entries.associateWith { it.toMessageMap() }

        @JvmStatic
        fun getLanguageKeyFrom(xs: String): LangKey =
            LangKey.entries.find {
                it.key.equals(xs, ignoreCase = true)
            } ?: ENGLISH
    }

    fun toMessageMap(): Map<MessageKey, String> {
        return when (this) {
            ENGLISH -> MessageKey.entries.associateWith { it.defaultMessage }
            KOREAN -> MessageKey.entries.associateWith(LangKey::getKoreanMessage)
        }
    }

    fun getJVMDefaultLanguage(): LangKey =
        if (Locale.getDefault().language == "ko_kr") {
            KOREAN
        } else ENGLISH
}