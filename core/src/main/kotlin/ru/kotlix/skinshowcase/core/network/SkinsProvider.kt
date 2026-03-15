package ru.kotlix.skinshowcase.core.network

import android.content.Context
import ru.kotlix.skinshowcase.core.database.DatabaseProvider
import ru.kotlix.skinshowcase.core.repository.SkinsRepository

/**
 * Провайдер [SkinsRepository] для использования из app без DI.
 * Данные через api-gateway (BASE_URL). Инициализировать в [android.app.Application.onCreate]:
 * [SkinsProvider.init][init].
 */
object SkinsProvider {

    private var appContext: Context? = null
    private var cachedRepository: SkinsRepository? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val repository: SkinsRepository
        get() {
            cachedRepository?.let { return it }
            val ctx = appContext
                ?: error("SkinsProvider not initialized. Call SkinsProvider.init(context) in Application.onCreate()")
            val api = RetrofitProvider.create(ApiService::class.java)
            val db = DatabaseProvider.create(ctx)
            return SkinsRepository(api, db.favoriteSkinDao()).also { cachedRepository = it }
        }
}
