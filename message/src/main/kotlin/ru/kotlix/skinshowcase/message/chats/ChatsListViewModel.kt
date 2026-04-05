package ru.kotlix.skinshowcase.message.chats

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.messaging.MessagingChatPaths
import ru.kotlix.skinshowcase.core.network.messaging.MessagingProvider
import ru.kotlix.skinshowcase.message.domain.ChatItem
import ru.kotlix.skinshowcase.message.domain.toChatItem

class ChatsListViewModel : BaseViewModel<ChatsListUiState>() {

    private val repository = MessagingProvider.repository

    override fun initialState(): ChatsListUiState = ChatsListUiState(
        isLoading = true,
        chats = pinnedSupportChat()
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
                        val withSupport = pinnedSupportChat() + fromApi.filter { chat ->
                            !MessagingChatPaths.isSupportMessagingSteamId(chat.id) &&
                                chat.id != SUPPORT_CHAT_ID
                        }
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
                            chats = pinnedSupportChat() + it.chats.filter { c ->
                                !MessagingChatPaths.isSupportMessagingSteamId(c.id) &&
                                    c.id != SUPPORT_CHAT_ID
                            },
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.throwable.bestApiMessage()
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
                                errorMessage = e.bestApiMessage()
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

    /**
     * Новый чат: 17-значный Steam ID или имя → GET /auth/users/by-username/{username}.
     */
    fun resolveChatRecipient(
        raw: String,
        onResolved: (steamId: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        launch {
            val t = raw.trim()
            if (t.isEmpty()) {
                onError("Введите Steam ID или имя")
                return@launch
            }
            if (MessagingChatPaths.isValidSteamId17(t)) {
                onResolved(t)
                return@launch
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java).getSteamIdByUsername(t).steamId.trim()
                }
            }.fold(
                onSuccess = { id ->
                    if (MessagingChatPaths.isValidSteamId17(id)) onResolved(id)
                    else onError("Пользователь не найден")
                },
                onFailure = { e -> onError(e.bestApiMessage()) }
            )
        }
    }

    fun deleteChat(chatId: String) {
        launch {
            when (val result = repository.deleteChat(chatId)) {
                is Result.Success -> updateState {
                    it.copy(chats = it.chats.filter { c -> c.id != chatId })
                }
                is Result.Error -> updateState {
                    it.copy(errorMessage = result.throwable.bestApiMessage())
                }
                is Result.Loading -> { }
            }
        }
    }

    companion object {
        /**
         * ID для навигации и закреплённой карточки: при валидном `MESSAGING_SUPPORT_STEAM_ID` в Gradle — он,
         * иначе плейсхолдер `support`. Сообщения на сервер уходят на [MessagingChatPaths.SUPPORT_MESSAGING_STEAM_ID].
         */
        val SUPPORT_CHAT_ID: String =
            if (MessagingChatPaths.isValidSteamId17(MessagingChatPaths.configuredSupportSteamId)) {
                MessagingChatPaths.configuredSupportSteamId
            } else {
                MessagingChatPaths.SUPPORT_PLACEHOLDER
            }

        fun isSupportChatId(chatId: String): Boolean =
            chatId == MessagingChatPaths.SUPPORT_PLACEHOLDER ||
                (SUPPORT_CHAT_ID != MessagingChatPaths.SUPPORT_PLACEHOLDER && chatId == SUPPORT_CHAT_ID) ||
                MessagingChatPaths.isSupportMessagingSteamId(chatId)
    }
}

/** Чат поддержки всегда закреплён сверху списка (как в продукте). */
private fun pinnedSupportChat(): List<ChatItem> = listOf(supportChatItem())

private fun supportChatItem(): ChatItem = ChatItem(
    id = ChatsListViewModel.SUPPORT_CHAT_ID,
    nickname = "Поддержка",
    lastMessage = "",
    lastMessageTimeMillis = 0L,
    unreadCount = 0,
    avatarUrl = null
)
