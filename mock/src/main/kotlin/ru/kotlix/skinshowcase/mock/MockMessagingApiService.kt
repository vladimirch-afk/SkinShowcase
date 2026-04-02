package ru.kotlix.skinshowcase.mock

import kotlinx.coroutines.delay
import ru.kotlix.skinshowcase.core.network.messaging.ChatDto
import ru.kotlix.skinshowcase.core.network.messaging.MessageDto
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

    override suspend fun getMessages(chatId: String): List<MessageDto> {
        delay(MOCK_DELAY_MS)
        return MockData.getMessages(chatId)
    }

    override suspend fun sendMessage(chatId: String, body: SendMessageRequest): MessageDto {
        delay(MOCK_DELAY_MS)
        val newMessage = MessageDto(
            id = "mock-msg-${System.currentTimeMillis()}",
            text = body.text,
            isOutgoing = true,
            timeMillis = System.currentTimeMillis()
        )
        MockData.addMessage(chatId, newMessage)
        return newMessage
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
