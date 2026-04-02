package ru.kotlix.skinshowcase.mock

import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.network.messaging.MessagingApiService

/**
 * Точка входа мок-сервера: API-сервисы и данные профиля/офферов.
 * Включение: [setEnabled] вызывается из Application при сборке с USE_MOCK_SERVER.
 */
object MockServer {

    @Volatile
    private var enabled: Boolean = false

    private val apiServiceInstance: ApiService by lazy { MockApiService() }
    private val messagingApiServiceInstance: MessagingApiService by lazy { MockMessagingApiService() }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun isEnabled(): Boolean = enabled

    fun getApiService(): ApiService = apiServiceInstance

    fun getMessagingApiService(): MessagingApiService = messagingApiServiceInstance

    fun getProfileData(): MockProfileData = MockProfileData(
        steamNickname = MockData.profileSteamNickname,
        steamAvatarUrl = MockData.profileSteamAvatarUrl,
        steamId = MockData.profileSteamId,
        activeOffers = MockData.getOffers().take(2),
        dealHistory = MockData.profileDeals
    )

    fun getOffers(): List<MockOfferSummary> = MockData.getOffers()

    /** Создаёт оффер по skinId; возвращает id нового оффера или null. */
    fun createOffer(skinId: String): String? = MockData.addOffer(skinId)

    /** Удаляет оффер по id. */
    fun deleteOffer(id: String): Boolean = MockData.removeOffer(id)

    /** Данные продавца для чужого оффера по skinId. */
    fun getSellerForSkin(skinId: String): MockSellerInfo = MockData.getSellerForSkin(skinId)
}
