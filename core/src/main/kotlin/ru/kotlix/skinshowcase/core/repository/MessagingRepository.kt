package ru.kotlix.skinshowcase.core.repository

import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.database.dao.ChatCacheDao
import ru.kotlix.skinshowcase.core.database.dao.MessageCacheDao
import ru.kotlix.skinshowcase.core.database.mapper.toCachedChatEntity
import ru.kotlix.skinshowcase.core.database.mapper.toCachedMessageEntity
import ru.kotlix.skinshowcase.core.database.mapper.toChatDto
import ru.kotlix.skinshowcase.core.database.mapper.toMessageDto
import ru.kotlix.skinshowcase.core.network.messaging.ChatDto
import ru.kotlix.skinshowcase.core.network.messaging.MessagingApiService
import ru.kotlix.skinshowcase.core.network.messaging.MessagingChatPaths
import ru.kotlix.skinshowcase.core.network.messaging.MessageDto
import ru.kotlix.skinshowcase.core.network.messaging.SendMessageRequest
import ru.kotlix.skinshowcase.core.network.messaging.toMessageDto

/**
 * Репозиторий сообщений: чаты и сообщения через [MessagingApiService] с кэшем в Room.
 */
class MessagingRepository(
    private val api: MessagingApiService,
    private val chatCacheDao: ChatCacheDao,
    private val messageCacheDao: MessageCacheDao
) {

    suspend fun getChats(): Result<List<ChatDto>> {
        return runCatching { api.getChats() }
            .fold(
                onSuccess = { list ->
                    chatCacheDao.deleteAll()
                    chatCacheDao.insertAll(list.map { it.toCachedChatEntity() })
                    Result.Success(list)
                },
                onFailure = {
                    val cached = chatCacheDao.getAll().map { it.toChatDto() }
                    Result.Success(cached)
                }
            )
    }

    suspend fun getMessages(chatId: String): Result<List<MessageDto>> {
        val apiChatId = MessagingChatPaths.steamIdForApiPath(chatId)
        return runCatching { api.getMessages(apiChatId) }
            .fold(
                onSuccess = { list ->
                    val dtos = list.map { it.toMessageDto() }
                    messageCacheDao.deleteByChatId(chatId)
                    messageCacheDao.insertAll(dtos.map { it.toCachedMessageEntity(chatId) })
                    Result.Success(dtos)
                },
                onFailure = {
                    val cached = messageCacheDao.getByChatId(chatId).map { it.toMessageDto() }
                    Result.Success(cached)
                }
            )
    }

    suspend fun sendMessage(chatId: String, text: String): Result<MessageDto> {
        val apiChatId = MessagingChatPaths.steamIdForApiPath(chatId)
        return runCatching { api.sendMessage(apiChatId, SendMessageRequest(text)) }
            .fold(
                onSuccess = { raw ->
                    val msg = raw.toMessageDto()
                    messageCacheDao.insert(msg.toCachedMessageEntity(chatId))
                    Result.Success(msg)
                },
                onFailure = { Result.Error(it) }
            )
    }

    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
        val apiChatId = MessagingChatPaths.steamIdForApiPath(chatId)
        return runCatching {
            api.deleteMessage(apiChatId, messageId)
            messageCacheDao.deleteMessage(chatId, messageId)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Error(it) }
        )
    }

    suspend fun deleteChat(chatId: String): Result<Unit> {
        val apiChatId = MessagingChatPaths.steamIdForApiPath(chatId)
        return runCatching {
            api.deleteChat(apiChatId)
            messageCacheDao.deleteByChatId(chatId)
            chatCacheDao.deleteByChatId(chatId)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Error(it) }
        )
    }
}
