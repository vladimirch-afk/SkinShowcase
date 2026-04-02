package ru.kotlix.skinshowcase.core.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException

/**
 * RFC 7807 Problem Details (application/problem+json), как отдаёт api-gateway.
 */
data class ProblemDetailDto(
    @SerializedName("type") val type: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("detail") val detail: String? = null,
    @SerializedName("instance") val instance: String? = null
)

private val problemDetailGson = Gson()

/**
 * Человекочитаемый текст: [ProblemDetailDto.detail] или title, иначе сообщение Retrofit/исключения.
 */
fun Throwable.bestApiMessage(): String {
    val http = this as? HttpException ?: return message?.takeIf { it.isNotBlank() } ?: toString()
    val raw = http.response()?.errorBody()?.use { it.string() }?.takeIf { it.isNotBlank() }
        ?: return http.message()
    val parsed = runCatching { problemDetailGson.fromJson(raw, ProblemDetailDto::class.java) }.getOrNull()
    val detail = parsed?.detail?.trim()?.takeIf { it.isNotEmpty() }
    val title = parsed?.title?.trim()?.takeIf { it.isNotEmpty() }
    return detail ?: title ?: http.message()
}
