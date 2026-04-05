package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/** Тело GET /auth/documents/{slug} (текст/HTML/MD в одном поле). */
data class LegalDocumentContentDto(
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("text") val text: String? = null
) {
    fun resolvedText(): String =
        content?.trim()?.takeIf { it.isNotEmpty() }
            ?: body?.trim()?.takeIf { it.isNotEmpty() }
            ?: text?.trim().orEmpty()
}
