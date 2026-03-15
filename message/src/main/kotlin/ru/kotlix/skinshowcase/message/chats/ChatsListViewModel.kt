package ru.kotlix.skinshowcase.message.chats

import ru.kotlix.skinshowcase.core.Result
import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.network.messaging.MessagingProvider
import ru.kotlix.skinshowcase.message.domain.ChatItem
import ru.kotlix.skinshowcase.message.domain.toChatItem

class ChatsListViewModel : BaseViewModel<ChatsListUiState>() {

    private val repository = MessagingProvider.repository

    override fun initialState(): ChatsListUiState = ChatsListUiState(isLoading = true)

    init {
        loadChats()
    }

    fun loadChats() {
        launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getChats()) {
                is Result.Success -> updateState {
                    it.copy(
                        chats = result.data.map { dto -> dto.toChatItem() },
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Result.Error -> updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.throwable.message ?: "Ошибка загрузки чатов"
                    )
                }
                is Result.Loading -> { }
            }
        }
    }
}
