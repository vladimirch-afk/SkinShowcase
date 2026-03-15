package ru.kotlix.skinshowcase.core.network.messaging

import android.content.Context
import ru.kotlix.skinshowcase.core.database.DatabaseProvider
import ru.kotlix.skinshowcase.core.repository.MessagingRepository
import ru.kotlix.skinshowcase.core.network.RetrofitProvider

/**
 * Провайдер [MessagingRepository] для использования из модуля message без DI.
 * Использует общий [RetrofitProvider] (BASE_URL) или переданный [MessagingApiService] (мок).
 * Кэш чатов и сообщений в Room — передайте [Context] в [init] для кэширования.
 */
object MessagingProvider {

    private var appContext: Context? = null
    private var injectedApi: MessagingApiService? = null
    private var cachedRepository: MessagingRepository? = null

    fun init(context: Context?, api: MessagingApiService? = null) {
        appContext = context?.applicationContext
        injectedApi = api
        cachedRepository = null
    }

    val repository: MessagingRepository
        get() {
            cachedRepository?.let { return it }
            val api = injectedApi ?: RetrofitProvider.create(MessagingApiService::class.java)
            val ctx = appContext
            val repo = if (ctx != null) {
                val db = DatabaseProvider.create(ctx)
                MessagingRepository(api, db.chatCacheDao(), db.messageCacheDao())
            } else {
                MessagingRepository(api, EmptyChatCacheDao(), EmptyMessageCacheDao())
            }
            return repo.also { cachedRepository = it }
        }
}

/** Заглушка DAO при отсутствии Context (тесты): кэш не используется. */
private class EmptyChatCacheDao : ru.kotlix.skinshowcase.core.database.dao.ChatCacheDao {
    override suspend fun getAll(): List<ru.kotlix.skinshowcase.core.database.entity.CachedChatEntity> = emptyList()
    override suspend fun insertAll(entities: List<ru.kotlix.skinshowcase.core.database.entity.CachedChatEntity>) {}
    override suspend fun deleteAll() {}
    override suspend fun deleteByChatId(chatId: String) {}
}

private class EmptyMessageCacheDao : ru.kotlix.skinshowcase.core.database.dao.MessageCacheDao {
    override suspend fun getByChatId(chatId: String): List<ru.kotlix.skinshowcase.core.database.entity.CachedMessageEntity> = emptyList()
    override suspend fun insertAll(entities: List<ru.kotlix.skinshowcase.core.database.entity.CachedMessageEntity>) {}
    override suspend fun insert(entity: ru.kotlix.skinshowcase.core.database.entity.CachedMessageEntity) {}
    override suspend fun deleteByChatId(chatId: String) {}
    override suspend fun deleteMessage(chatId: String, messageId: String) {}
}
