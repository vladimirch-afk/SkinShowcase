package ru.kotlix.skinshowcase.message.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.ReportUserRequestDto
import ru.kotlix.skinshowcase.core.network.messaging.MessagingChatPaths
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

    private val reportTargetSteamId: String? =
        if (ChatsListViewModel.isSupportChatId(chatId)) {
            null
        } else {
            MessagingChatPaths.steamIdForApiPath(chatId).trim().takeIf {
                MessagingChatPaths.isValidSteamId17(it)
            }
        }

    private val _uiState = MutableStateFlow(
        ChatUiState(
            chatTitle = if (ChatsListViewModel.isSupportChatId(chatId)) "Поддержка" else "Чат $chatId",
            canReportCounterparty = reportTargetSteamId != null
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
                        errorMessage = result.throwable.bestApiMessage()
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
                        errorMessage = result.throwable.bestApiMessage()
                    )
                }
                is Result.Loading -> { }
            }
        }
    }

    fun getChatId(): String = chatId

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            val target = _uiState.value.messages.find { it.id == messageId }
            if (target == null || !target.isOutgoing) return@launch
            when (val result = repository.deleteMessage(chatId, messageId)) {
                is Result.Success -> _uiState.update {
                    it.copy(messages = it.messages.filter { m -> m.id != messageId })
                }
                is Result.Error -> _uiState.update {
                    it.copy(errorMessage = result.throwable.bestApiMessage())
                }
                is Result.Loading -> { }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Жалоба на собеседника: [POST auth/users/{steamId}/report][AuthApiService.reportUser].
     */
    fun reportCounterparty(reason: String, details: String?, onComplete: (errorMessage: String?) -> Unit) {
        val steamId = reportTargetSteamId
        if (steamId == null) {
            onComplete("Жалоба для этого чата недоступна")
            return
        }
        val r = reason.trim()
        if (r.isEmpty()) {
            onComplete("Укажите причину")
            return
        }
        viewModelScope.launch {
            val err = runCatching {
                withContext(Dispatchers.IO) {
                    val contextBlock = buildString {
                        append("Жалоба из чата.")
                        append("\nchatId (клиент): ")
                        append(chatId)
                        val extra = details?.trim()?.takeIf { it.isNotEmpty() }
                        if (extra != null) {
                            append("\n\n")
                            append(extra)
                        }
                    }
                    RetrofitProvider.create(AuthApiService::class.java).reportUser(
                        steamId,
                        ReportUserRequestDto(
                            reason = r,
                            details = contextBlock.trim().takeIf { it.isNotEmpty() }
                        )
                    )
                }
            }.exceptionOrNull()
            onComplete(err?.bestApiMessage())
        }
    }

    companion object {
        const val CHAT_ID_ARG = "chatId"
    }
}
