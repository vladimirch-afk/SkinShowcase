package ru.kotlix.skinshowcase.core.network

import android.content.Context
import ru.kotlix.skinshowcase.core.database.DatabaseProvider
import ru.kotlix.skinshowcase.core.network.inventory.InventoryApiService
import ru.kotlix.skinshowcase.core.repository.SkinsRepository

/**
 * Провайдер [SkinsRepository] для использования из app без DI.
 * Данные через api-gateway (BASE_URL) или переданный [ApiService] (мок).
 * Инициализировать в [android.app.Application.onCreate]: [SkinsProvider.init][init].
 */
object SkinsProvider {

    private var appContext: Context? = null
    private var injectedApiService: ApiService? = null
    private var cachedRepository: SkinsRepository? = null

    fun init(context: Context, apiService: ApiService? = null) {
        appContext = context.applicationContext
        injectedApiService = apiService
        cachedRepository = null
    }

    val repository: SkinsRepository
        get() {
            cachedRepository?.let { return it }
            val ctx = appContext
                ?: error("SkinsProvider not initialized. Call SkinsProvider.init(context) in Application.onCreate()")
            val api = injectedApiService ?: RetrofitProvider.create(ApiService::class.java)
            val inventoryApi = RetrofitProvider.create(InventoryApiService::class.java)
            val db = DatabaseProvider.create(ctx)
            return SkinsRepository(api, inventoryApi, db.favoriteSkinDao(), db.skinCacheDao()).also { cachedRepository = it }
        }
}
