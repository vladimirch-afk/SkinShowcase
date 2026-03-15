package ru.kotlix.skinshowcase.message.chats

import ru.kotlix.skinshowcase.message.domain.ChatItem

data class ChatsListUiState(
    val chats: List<ChatItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
