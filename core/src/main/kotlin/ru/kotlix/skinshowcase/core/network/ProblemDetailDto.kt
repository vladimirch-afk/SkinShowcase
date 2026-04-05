package ru.kotlix.skinshowcase.core.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException
import java.util.Locale

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
        ?: return humanizeHttpFallback(http.code(), http.message())
    val parsed = runCatching { problemDetailGson.fromJson(raw, ProblemDetailDto::class.java) }.getOrNull()
    val detail = parsed?.detail?.trim()?.takeIf { it.isNotEmpty() }
    val title = parsed?.title?.trim()?.takeIf { it.isNotEmpty() }
    humanizeProblemDetail(http.code(), detail, title)?.let { return it }
    return detail ?: title ?: humanizeHttpFallback(http.code(), http.message())
}

/**
 * Понятные тексты для типичных Problem Details (messaging, auth через gateway).
 */
private fun humanizeProblemDetail(httpCode: Int, detail: String?, title: String?): String? {
    val d = detail?.lowercase(Locale.ROOT) ?: ""
    if (httpCode == 404) {
        if (d.startsWith("user not found")) {
            return "Пользователь с таким именем или Steam ID не найден."
        }
    }
    if (httpCode == 400) {
        if (d.contains("cannot get chat with yourself")) {
            return "Нельзя открыть чат с самим собой."
        }
        if (d.contains("cannot send message to yourself")) {
            return "Нельзя отправить сообщение самому себе."
        }
        if (d.contains("invalid steam id")) {
            return "Некорректный Steam ID: нужна строка из 17 цифр."
        }
    }
    if (httpCode == 500) {
        if (d == "internal server error" || d.isBlank()) {
            return "Ошибка сервера. Попробуйте позже."
        }
    }
    return null
}

private fun humanizeHttpFallback(httpCode: Int, retrofitMessage: String): String =
    when (httpCode) {
        404 -> "Не найдено."
        400 -> "Некорректный запрос."
        500 -> "Ошибка сервера. Попробуйте позже."
        else -> retrofitMessage
    }
