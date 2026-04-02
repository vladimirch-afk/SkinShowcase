package ru.kotlix.skinshowcase.core.network

import com.google.gson.annotations.SerializedName

/**
 * Пагинированный ответ api-gateway → items (GET /api/v1/items).
 */
data class PageResponseDto<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("totalElements") val totalElements: Long = 0L,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("size") val size: Int = 0,
    @SerializedName("number") val number: Int = 0
)
