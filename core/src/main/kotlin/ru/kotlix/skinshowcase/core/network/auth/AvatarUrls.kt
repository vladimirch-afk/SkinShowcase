package ru.kotlix.skinshowcase.core.network.auth

import java.util.Locale
import ru.kotlix.skinshowcase.core.network.ApiConfig

/**
 * Публичные URL пресетных аватарок: GET /auth/avatars и GET /auth/avatars/{id} через api-gateway.
 */
object AvatarUrls {

    /** Если в профиле нет выбранной аватарки — показываем первый пресет. */
    const val DEFAULT_PRESET_ID = "1"

    fun presetImageUrl(presetId: String): String {
        val base = ApiConfig.BASE_URL.trimEnd('/')
        val id = presetId.trim().ifEmpty { DEFAULT_PRESET_ID }
        return "$base/auth/avatars/$id"
    }

    fun userAvatarUrl(avatarPresetId: String?): String {
        val id = avatarPresetId?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_PRESET_ID
        return presetImageUrl(id)
    }

    /** URL для шапки/офферов после [ProfileDataProvider.syncCurrentUserFromAuthMe]. */
    fun currentUserAvatarDisplayUrl(): String {
        val src = CurrentUser.avatarSource?.trim()?.uppercase(Locale.US)
        if (src == "STEAM") {
            val u = CurrentUser.steamAvatarUrl?.trim()?.takeIf { it.isNotEmpty() }
            if (u != null) return u
            return presetImageUrl(DEFAULT_PRESET_ID)
        }
        return userAvatarUrl(CurrentUser.avatarPresetId)
    }
}
