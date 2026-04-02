package ru.kotlix.skinshowcase.core.network

import com.google.gson.annotations.SerializedName

/**
 * Ответ ручки GET /api/v1/items/{itemId}/screenshot (OpenAPI).
 */
data class ScreenshotResponseDto(
    @SerializedName("screenshotUrl") val screenshotUrl: String
)
