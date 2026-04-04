package ru.kotlix.skinshowcase.message.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.network.messaging.MessagingProvider
import ru.kotlix.skinshowcase.message.chats.ChatsListViewModel
import ru.kotlix.skinshowcase.message.domain.MessageItem
import ru.kotlix.skinshowcase.message.domain.toMessageItem

class ChatViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private fun List<MessageItem>.sortedChronologically(): List<MessageItem> =
        sortedWith(compareBy({ it.timeMillis }, { it.id }))

    private val chatId: String = savedStateHandle.get<String>(CHAT_ID_ARG) ?: ""
    private val repository = MessagingProvider.repository

    private val _uiState = MutableStateFlow(
        ChatUiState(
            chatTitle = if (ChatsListViewModel.isSupportChatId(chatId)) "Поддержка" else "Чат $chatId"
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getMessages(chatId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        messages = result.data.map { dto -> dto.toMessageItem() }.sortedChronologically(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.throwable.message ?: "Ошибка загрузки сообщений"
                    )
                }
                is Result.Loading -> { }
            }
        }
    }

    fun updateDraft(text: String) {
        _uiState.update { it.copy(messageDraft = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.messageDraft.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(messageDraft = "", isSending = true, errorMessage = null)
            }
            when (val result = repository.sendMessage(chatId, text)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        messages = (it.messages + result.data.toMessageItem()).sortedChronologically(),
                        isSending = false
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(
                        messageDraft = text,
                        isSending = false,
                        errorMessage = result.throwable.message ?: "Ошибка отправки"
                    )
                }
                is Result.Loading -> { }
            }
        }
    }

    fun getChatId(): String = chatId

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteMessage(chatId, messageId)) {
                is Result.Success -> _uiState.update {
                    it.copy(messages = it.messages.filter { m -> m.id != messageId })
                }
                is Result.Error -> _uiState.update {
                    it.copy(errorMessage = result.throwable.message ?: "Ошибка удаления")
                }
                is Result.Loading -> { }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        const val CHAT_ID_ARG = "chatId"
    }
}
