package ru.kotlix.skinshowcase.mock

import java.time.Instant
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import ru.kotlix.skinshowcase.core.network.messaging.ChatDto
import ru.kotlix.skinshowcase.core.network.messaging.MessageDto
import ru.kotlix.skinshowcase.core.network.messaging.MessageResponseDto
import ru.kotlix.skinshowcase.core.network.messaging.MessagingApiService
import ru.kotlix.skinshowcase.core.network.messaging.SendMessageRequest

/**
 * Мок [MessagingApiService]: чаты и сообщения из [MockData].
 */
class MockMessagingApiService : MessagingApiService {

    override suspend fun getChats(): List<ChatDto> {
        delay(MOCK_DELAY_MS)
        return MockData.getChats()
    }

    override suspend fun getChatByUsername(username: String): ChatDto {
        delay(MOCK_DELAY_MS)
        val t = username.trim()
        if (t == MockData.profileSteamId) {
            throwHttp(400, "Cannot get chat with yourself")
        }
        for (c in MockData.getChats()) {
            if (c.counterpartySteamId == t) return c
            if (c.counterpartyNickname?.equals(t, ignoreCase = true) == true) return c
        }
        if (t.length == 17 && t.all { it.isDigit() }) {
            return ChatDto(
                counterpartySteamId = t,
                counterpartyNickname = t,
                lastMessagePreview = "",
                lastMessageAt = Instant.EPOCH.toString(),
                avatarUrl = null
            )
        }
        throwUserNotFound(t)
    }

    override suspend fun getMessages(
        chatId: String,
        page: Int,
        size: Int
    ): List<MessageResponseDto> {
        delay(MOCK_DELAY_MS)
        val me = MockData.profileSteamId
        return MockData.getMessages(chatId).map { m ->
            MessageResponseDto(
                id = m.id,
                senderSteamId = if (m.isOutgoing) me else chatId,
                recipientSteamId = if (m.isOutgoing) chatId else me,
                text = m.text,
                createdAt = Instant.ofEpochMilli(m.timeMillis).toString()
            )
        }
    }

    override suspend fun sendMessage(chatId: String, body: SendMessageRequest): MessageResponseDto {
        delay(MOCK_DELAY_MS)
        val id = "mock-msg-${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        val response = MessageResponseDto(
            id = id,
            senderSteamId = MockData.profileSteamId,
            recipientSteamId = chatId,
            text = body.text,
            createdAt = Instant.ofEpochMilli(now).toString()
        )
        MockData.addMessage(
            chatId,
            MessageDto(id = id, text = body.text, isOutgoing = true, timeMillis = now)
        )
        return response
    }

    override suspend fun deleteMessage(chatId: String, messageId: String) {
        delay(MOCK_DELAY_MS)
        MockData.removeMessage(chatId, messageId)
    }

    override suspend fun deleteChat(chatId: String) {
        delay(MOCK_DELAY_MS)
        MockData.removeChat(chatId)
    }

    private companion object {
        const val MOCK_DELAY_MS = 100L

        private fun throwUserNotFound(username: String): Nothing {
            val safe = username.replace("\\", "\\\\").replace("\"", "\\\"")
            val json =
                """{"title":"Not Found","status":404,"detail":"User not found: $safe"}"""
            val body = json.toResponseBody("application/problem+json".toMediaType())
            throw HttpException(Response.error<ChatDto>(404, body))
        }

        private fun throwHttp(code: Int, detail: String): Nothing {
            val safe = detail.replace("\\", "\\\\").replace("\"", "\\\"")
            val json = """{"title":"Error","status":$code,"detail":"$safe"}"""
            val body = json.toResponseBody("application/problem+json".toMediaType())
            throw HttpException(Response.error<ChatDto>(code, body))
        }
    }
}
