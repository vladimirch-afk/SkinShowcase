package ru.kotlix.skinshowcase.core.network.auth

import com.google.gson.annotations.SerializedName

/** Метаданные документа GET /auth/documents */
data class LegalDocumentSummaryDto(
    @SerializedName("slug") val slug: String,
    @SerializedName("title") val title: String,
    @SerializedName("updatedAt") val updatedAt: String? = null
)
