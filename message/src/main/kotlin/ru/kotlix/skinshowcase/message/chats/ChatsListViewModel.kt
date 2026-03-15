package ru.kotlix.skinshowcase.message.chats

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.messaging.MessagingProvider
import ru.kotlix.skinshowcase.message.domain.ChatItem
import ru.kotlix.skinshowcase.message.domain.toChatItem

class ChatsListViewModel : BaseViewModel<ChatsListUiState>() {

    private val repository = MessagingProvider.repository

    override fun initialState(): ChatsListUiState = ChatsListUiState(
        isLoading = true,
        chats = listOf(supportChatItem())
    )

    init {
        loadChats()
    }

    fun loadChats() {
        launch {
            updateState { state ->
                val hadChats = state.chats.isNotEmpty()
                state.copy(
                    isLoading = !hadChats,
                    isRefreshing = hadChats,
                    errorMessage = null
                )
            }
            try {
                val result = withTimeout(15_000) {
                    withContext(Dispatchers.IO) {
                        repository.getChats()
                    }
                }
                when (result) {
                    is Result.Success -> {
                        val fromApi = result.data.map { dto -> dto.toChatItem() }
                        val withSupport = listOf(supportChatItem()) + fromApi.filter { it.id != SUPPORT_CHAT_ID }
                        updateState {
                            it.copy(
                                chats = withSupport,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Result.Error -> updateState {
                        it.copy(
                            chats = listOf(supportChatItem()) + it.chats.filter { c -> c.id != SUPPORT_CHAT_ID },
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.throwable.message ?: "Ошибка загрузки чатов"
                        )
                    }
                    is Result.Loading -> updateState {
                        it.copy(isLoading = false, isRefreshing = false)
                    }
                }
            } catch (e: Throwable) {
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main.immediate) {
                        updateState {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = e.message ?: "Ошибка загрузки чатов"
                            )
                        }
                    }
                }
                if (e is CancellationException) throw e
            } finally {
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main.immediate) {
                        updateState { it.copy(isLoading = false, isRefreshing = false) }
                    }
                }
            }
        }
    }

    fun clearRefreshing() {
        updateState { it.copy(isRefreshing = false) }
    }

    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    companion object {
        const val SUPPORT_CHAT_ID = "support"
    }
}

private fun supportChatItem(): ChatItem = ChatItem(
    id = ChatsListViewModel.SUPPORT_CHAT_ID,
    title = "Поддержка",
    lastMessage = "",
    lastMessageTimeMillis = 0L,
    unreadCount = 0
)
