package ru.kotlix.skinshowcase.core.network.messaging

import ru.kotlix.skinshowcase.core.repository.MessagingRepository
import ru.kotlix.skinshowcase.core.network.RetrofitProvider

/**
 * Провайдер [MessagingRepository] для использования из модуля message без DI.
 * Использует общий [RetrofitProvider] (BASE_URL из [ApiConfig]).
 */
object MessagingProvider {

    val repository: MessagingRepository by lazy {
        val api = RetrofitProvider.create(MessagingApiService::class.java)
        MessagingRepository(api)
    }
}
