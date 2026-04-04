package ru.kotlix.skinshowcase.mock

import java.time.Instant
import kotlinx.coroutines.delay
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

    override suspend fun getMessages(chatId: String): List<MessageResponseDto> {
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
    }
}
