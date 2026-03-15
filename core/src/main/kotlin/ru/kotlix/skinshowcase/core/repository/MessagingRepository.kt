package ru.kotlix.skinshowcase.core.repository

import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.network.messaging.ChatDto
import ru.kotlix.skinshowcase.core.network.messaging.MessagingApiService
import ru.kotlix.skinshowcase.core.network.messaging.MessageDto
import ru.kotlix.skinshowcase.core.network.messaging.SendMessageRequest

/**
 * Репозиторий сообщений: чаты и сообщения через [MessagingApiService].
 * Ошибки сети возвращаются как [Result.Error].
 */
class MessagingRepository(
    private val api: MessagingApiService
) {

    suspend fun getChats(): Result<List<ChatDto>> = runCatchingApi { api.getChats() }

    suspend fun getMessages(chatId: String): Result<List<MessageDto>> =
        runCatchingApi { api.getMessages(chatId) }

    suspend fun sendMessage(chatId: String, text: String): Result<MessageDto> =
        runCatchingApi { api.sendMessage(chatId, SendMessageRequest(text)) }

    private suspend fun <T> runCatchingApi(block: suspend () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
