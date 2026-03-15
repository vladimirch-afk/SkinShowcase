package ru.kotlix.skinshowcase.core.network.messaging

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessagingApiService {

    @GET("api/chats")
    suspend fun getChats(): List<ChatDto>

    @GET("api/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: String): List<MessageDto>

    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body body: SendMessageRequest
    ): MessageDto

    @DELETE("api/chats/{chatId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("chatId") chatId: String,
        @Path("messageId") messageId: String
    )

    @DELETE("api/chats/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: String)
}
