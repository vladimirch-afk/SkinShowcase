package ru.kotlix.skinshowcase.core.network.auth

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

private data class JwtPayload(
    @SerializedName("sub") val sub: String?
)

/**
 * Извлекает subject (SteamID64) из JWT без проверки подписи — только для отображения на клиенте;
 * доверие к сессии обеспечивает сервер при выдаче токена.
 */
object JwtSubjectParser {

    private val gson = Gson()

    fun parseSteamId(jwt: String): String? {
        val parts = jwt.split('.')
        if (parts.size < 2) return null
        return try {
            val decoded = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            val json = String(decoded, Charsets.UTF_8)
            gson.fromJson(json, JwtPayload::class.java).sub
        } catch (_: Exception) {
            null
        }
    }
}
