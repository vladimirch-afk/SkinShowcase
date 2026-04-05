package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/**
 * Тело PATCH /auth/me/avatar:
 * - `{"source":"STEAM"}`
 * - `{"source":"PRESET","presetAvatarId":1}` … `8`
 */
data class PatchMyAvatarRequestDto(
    @SerializedName("source") val source: String,
    @SerializedName("presetAvatarId") val presetAvatarId: Int? = null,
) {
    companion object {
        fun steam(): PatchMyAvatarRequestDto = PatchMyAvatarRequestDto("STEAM", null)

        fun preset(presetAvatarId: Int): PatchMyAvatarRequestDto =
            PatchMyAvatarRequestDto("PRESET", presetAvatarId)
    }
}
