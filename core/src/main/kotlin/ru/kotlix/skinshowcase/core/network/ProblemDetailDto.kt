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

/** Сообщение при HTTP 403 от API (заблокированный аккаунт). */
const val HTTP_FORBIDDEN_BLOCKED_USER_MESSAGE =
    "Ваш аккаунт заблокирован. Обратитесь в поддержку."

/** Сообщение при HTTP 409 на POST `/auth/users/{steamId}/report` (повторная жалоба). */
const val HTTP_CONFLICT_RECENT_USER_REPORT_MESSAGE =
    "Вы недавно жаловались на этого пользователя."

/** Любой [HttpException] с кодом 403 в цепочке причин (например обёртка). */
fun Throwable.isApiForbidden(): Boolean = httpExceptionInChain()?.code() == 403

private fun Throwable.httpExceptionInChain(): HttpException? {
    var cur: Throwable? = this
    val seen = hashSetOf<Throwable>()
    while (cur != null && seen.add(cur)) {
        if (cur is HttpException) return cur
        cur = cur.cause
    }
    return null
}

/**
 * Человекочитаемый текст: при 403 — [HTTP_FORBIDDEN_BLOCKED_USER_MESSAGE];
 * при 409 — [HTTP_CONFLICT_RECENT_USER_REPORT_MESSAGE] (дубликат жалобы);
 * иначе [ProblemDetailDto.detail] или title, иначе сообщение Retrofit/исключения.
 */
fun Throwable.bestApiMessage(): String {
    val http = httpExceptionInChain()
    if (http != null && http.code() == 403) {
        return HTTP_FORBIDDEN_BLOCKED_USER_MESSAGE
    }
    if (http != null && http.code() == 409) {
        return HTTP_CONFLICT_RECENT_USER_REPORT_MESSAGE
    }
    if (http == null) {
        return message?.takeIf { it.isNotBlank() } ?: toString()
    }
    val raw = http.response()?.errorBody()?.use { it.string() }?.takeIf { it.isNotBlank() }
        ?: return http.message()
    val parsed = runCatching { problemDetailGson.fromJson(raw, ProblemDetailDto::class.java) }.getOrNull()
    val detail = parsed?.detail?.trim()?.takeIf { it.isNotEmpty() }
    val title = parsed?.title?.trim()?.takeIf { it.isNotEmpty() }
    return detail ?: title ?: http.message()
}
