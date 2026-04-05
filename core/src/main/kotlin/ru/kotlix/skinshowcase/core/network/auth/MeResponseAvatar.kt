package ru.kotlix.skinshowcase.core.network.auth

import java.util.Locale

private const val SRC_STEAM = "STEAM"
private const val SRC_PRESET = "PRESET"

/** Нормализованный источник аватарки из GET /auth/me. */
fun MeResponseDto.avatarSourceNormalized(): String? =
    source?.trim()?.uppercase(Locale.US)?.takeIf { it.isNotEmpty() }

/**
 * URL для отображения аватарки по ответу /auth/me.
 * STEAM — [steamAvatarUrl], если сервер отдал; иначе запасной пресет.
 * PRESET — GET /auth/avatars/{id} по [presetAvatarId] (1–8) или устаревшему [avatarPresetId].
 */
fun MeResponseDto.resolveAvatarDisplayUrl(): String {
    val src = avatarSourceNormalized()
    when (src) {
        SRC_STEAM -> {
            val u = steamAvatarUrl?.trim()?.takeIf { it.isNotEmpty() }
            if (u != null) return u
            return AvatarUrls.presetImageUrl(AvatarUrls.DEFAULT_PRESET_ID)
        }
        SRC_PRESET -> {
            val n = presetAvatarId ?: avatarPresetId?.trim()?.toIntOrNull() ?: 1
            val id = n.coerceIn(1, 8).toString()
            return AvatarUrls.presetImageUrl(id)
        }
        else -> {
            val legacy = avatarPresetId?.trim()?.takeIf { it.isNotEmpty() }
            return AvatarUrls.userAvatarUrl(legacy)
        }
    }
}
