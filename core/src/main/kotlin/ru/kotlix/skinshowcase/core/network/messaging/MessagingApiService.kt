package ru.kotlix.skinshowcase.core.network.messaging

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * REST API сервиса сообщений.
 * Базовый URL задаётся в [ApiConfig] (тот же или отдельный MESSAGING_BASE_URL).
 *
 * Ожидаемые эндпоинты бэкенда:
 * - GET /chats — список чатов
 * - GET /chats/{chatId}/messages — сообщения чата
 * - POST /chats/{chatId}/messages — отправить сообщение
 */
interface MessagingApiService {

    @GET("chats")
    suspend fun getChats(): List<ChatDto>

    @GET("chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: String): List<MessageDto>

    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body body: SendMessageRequest
    ): MessageDto
}
