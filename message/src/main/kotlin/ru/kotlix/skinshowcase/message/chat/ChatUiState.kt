package ru.kotlix.skinshowcase.message.chat

import ru.kotlix.skinshowcase.message.domain.MessageItem

data class ChatUiState(
    val chatTitle: String = "",
    val messages: List<MessageItem> = emptyList(),
    val messageDraft: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    /** Есть валидный Steam ID собеседника для POST /auth/users/{steamId}/report. */
    val canReportCounterparty: Boolean = false
)
