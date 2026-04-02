package ru.kotlix.skinshowcase.mock

import ru.kotlix.skinshowcase.core.network.SkinDto
import ru.kotlix.skinshowcase.core.network.messaging.ChatDto
import ru.kotlix.skinshowcase.core.network.messaging.MessageDto

/**
 * Централизованные мок-данные для всех обращений к серверу.
 * Скины — реальные бюджетные скины CS2 с рыночными ценами до 1000 ₽.
 * Изображения — иконки Steam CDN (community.cloudflare.steamstatic.com).
 */
object MockData {

    private const val IMAGE_BASE = "https://picsum.photos/seed"

    /** Базовый URL иконок предметов Steam (CS2). */
    private const val STEAM_CDN = "https://community.cloudflare.steamstatic.com/economy/image"

    /** Реальные дешёвые скины CS2 (рыночные цены до 1000 ₽), иконки Steam CDN. */
    private val skinImageUrls: Map<String, String> = mapOf(
        "ak47-safari-mesh" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpot7HxfDhzw8zFdC5K08i3mr-GkvP9JrafxTlSvJYg2u_Erdn33wLh80JvMmylJoLAegA5ZVvR_QO6levq0cS_uIOJlyWN3DX90A",
        "ak47-elite-build" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpot7HxfDhjxszJemkV09G3h5SOhe7LO77QgHIfupQji7rCptmh3QHl_0plNWvwctXBc1c3YFmE8gPqkri8gcW5vsiYm2wj5HemP_sktw",
        "m4a4-urban-ddpat" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpou-6kejhoyszMdS1D-OOjhoK0n_L1JaKfxTNSuMZzj7CW8dTx0ADjqhZkNmD0Io7HIQ49NwzQrwW3wevv1J_utYOJlyXAEUAeYg",
        "awp-safari-mesh" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpot621FBRw7P7NYjV9-N24q42Ok_7hPvWAlTgBupwk0-3H8Nil3lLt_UBlYWnxI4WSdAU8NVyDr1S8w-a-g5O0ot2Xns69tUxy",
        "glock-18-ironwork" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgposbaqKAxf0uL3djFN79eJkIGZnLryMrfdqWNU6dNoxLqVpN_33lLm_xFkZGyhLY7BdVdtNQvW-FDtyOzmjMXotJvAnSZi6SI8pSGKHQdgnVU",
        "usp-s-lead-conduit" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpoo6m1FBRp3_bGcjhQ09ulq5WYh8jiPLfFl2xU18h0juDU-MKj2VXhrkA_MmnwJIGRdVA4Ml3XrAPvw-q9g5O8vc_Iy3cw7CVw5y3cgVXp1sQwp5E7",
        "p250-boreal-forest" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpopujwezhoyszOfi9H_8iJlo-Zkvb4DLPUl31Ipscj2LiR84rx3QzmqBc4Z2zzdo-dJg47NFnT8wPqw7zmhsK4uZWdz3N9-n51xJZZiIs",
        "deagle-mudder" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgposr-kLAtl7PvRTjBN-Mi6kYS0hPb6N4Tck29Y_cg_i76VpoqsjgDgrkA_aj2gLYKWJlQ8NFvUqwfrku3qgcS96J-bz3cyvT5iuyh2Lvz45A",
        "five-seven-forest-night" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgposLOzLhRlxfbGTjVb09q5hoWYg8j6OrzZglRZ7cRnk6fFpdWmjAzh_hdpa2v3LYTBIFJqZlqCqQS3lO6-hpfpvc7NyCdrvCck-z-DyLxq_vQS",
        "tec9-avalanche" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpoor-mcjhjxszcdD4b092glYyKmfT8NoTZk2pH8YtwjOuWot-g2wy1_0BtNW73LY6cdwQ4NwqE_gW_xri5gcW77svNwSM3pGB8stkM4xtR",
        "p90-elite-build" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpopuP1FAR17OORIQJP7c-ikZKSqPrxN7LEmyVT6pN3jurHpoj00AC1qEJvZzv7J4GXc1RqNV2G-1K9wujqgsfpvJ7L1zI97QbkaYR2",
        "ump45-gunsmoke" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpoo7e1f1Jf2-r3djhO_Nm4q42Ok_7hPvWHxGhXuMNz3biVpI6s3VHj8hBsZDr1ddLDJw82MAnX8gDowL-915a9ot2XnkfBduLo",
        "nova-graphite" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpouLWzKjhh3szLYyhP_NCzq4yCkP_gfb7QxmlQvJci3uzFodnxiwDi-EtlYjjxIdLBcwM7Z1nR_FbswLu7gJSi_MOe50rfWHI",
        "ssg08-slashed" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpopamie19f0Ob3Yi5FvISJnY2GmOXgMrfum25V4dB8xLGZp9zzjg3t-0dsa2j3JNfGdQZoYl6D_wW-w7y61sW7vZWayns36HY8pSGK_gLmvgk",
        "mac10-lapis-gator" to "$STEAM_CDN/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpou7umeldf0v73fDxBvYyJkYyOlOPmOrjYgnJu4MBwnPCPrN3z2APhrRJlMjr2LI6Uc1BvM1jY-wS9yLu6g5O_6JSbm3c1uyQn52GdwUKTdcDuUA"
    )

    /** Скины с ориентировочными рыночными ценами (до 1000 ₽). */
    val skins: List<SkinDto> = listOf(
        skinDto("ak47-safari-mesh", "AK-47 | Safari Mesh", skinImageUrls["ak47-safari-mesh"]!!, 28.0, 0.45, "The Arms Deal Collection", "MIL_SPEC", "WW", "NORMAL", 1, emptyList()),
        skinDto("ak47-elite-build", "AK-47 | Elite Build", skinImageUrls["ak47-elite-build"]!!, 220.0, 0.72, "The Chroma Collection", "CLASSIFIED", "BS", "NORMAL", 2, emptyList()),
        skinDto("m4a4-urban-ddpat", "M4A4 | Urban DDPAT", skinImageUrls["m4a4-urban-ddpat"]!!, 185.0, 0.68, "The Dust Collection", "MIL_SPEC", "BS", "NORMAL", 3, emptyList()),
        skinDto("awp-safari-mesh", "AWP | Safari Mesh", skinImageUrls["awp-safari-mesh"]!!, 480.0, 0.28, "The Arms Deal Collection", "MIL_SPEC", "FT", "NORMAL", 4, emptyList()),
        skinDto("glock-18-ironwork", "Glock-18 | Ironwork", skinImageUrls["glock-18-ironwork"]!!, 65.0, 0.82, "The Assault Collection", "INDUSTRIAL", "BS", "NORMAL", 5, emptyList()),
        skinDto("usp-s-lead-conduit", "USP-S | Lead Conduit", skinImageUrls["usp-s-lead-conduit"]!!, 95.0, 0.42, "The Assault Collection", "MIL_SPEC", "WW", "NORMAL", 6, emptyList()),
        skinDto("p250-boreal-forest", "P250 | Boreal Forest", skinImageUrls["p250-boreal-forest"]!!, 25.0, 0.78, "The Arms Deal Collection", "MIL_SPEC", "BS", "NORMAL", 7, emptyList()),
        skinDto("deagle-mudder", "Desert Eagle | Mudder", skinImageUrls["deagle-mudder"]!!, 130.0, 0.38, "The Assault Collection", "INDUSTRIAL", "WW", "NORMAL", 8, emptyList()),
        skinDto("five-seven-forest-night", "Five-SeveN | Forest Night", skinImageUrls["five-seven-forest-night"]!!, 52.0, 0.85, "The Arms Deal Collection", "MIL_SPEC", "BS", "NORMAL", 9, emptyList()),
        skinDto("tec9-avalanche", "Tec-9 | Avalanche", skinImageUrls["tec9-avalanche"]!!, 42.0, 0.76, "The Assault Collection", "MIL_SPEC", "BS", "NORMAL", 10, emptyList()),
        skinDto("p90-elite-build", "P90 | Elite Build", skinImageUrls["p90-elite-build"]!!, 125.0, 0.22, "The Chroma Collection", "CLASSIFIED", "FT", "NORMAL", 11, emptyList()),
        skinDto("ump45-gunsmoke", "UMP-45 | Gunsmoke", skinImageUrls["ump45-gunsmoke"]!!, 68.0, 0.41, "The Assault Collection", "MIL_SPEC", "WW", "NORMAL", 12, emptyList()),
        skinDto("nova-graphite", "Nova | Graphite", skinImageUrls["nova-graphite"]!!, 85.0, 0.00, "The Arms Deal Collection", "MIL_SPEC", "FN", "NORMAL", 13, emptyList()),
        skinDto("ssg08-slashed", "SSG 08 | Slashed", skinImageUrls["ssg08-slashed"]!!, 75.0, 0.39, "The Assault Collection", "INDUSTRIAL", "WW", "NORMAL", 14, emptyList()),
        skinDto("mac10-lapis-gator", "MAC-10 | Lapis Gator", skinImageUrls["mac10-lapis-gator"]!!, 98.0, 0.91, "The Chroma 2 Collection", "MIL_SPEC", "BS", "NORMAL", 15, emptyList())
    )

    private fun skinDto(
        id: String,
        name: String,
        imageUrl: String,
        price: Double,
        floatValue: Double,
        collection: String,
        rarity: String,
        wear: String,
        special: String,
        patternIndex: Int,
        stickerNames: List<String>,
        keychainNames: List<String>? = null
    ): SkinDto = SkinDto(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        floatValue = floatValue,
        stickerNames = stickerNames.ifEmpty { null },
        collection = collection,
        rarity = rarity,
        wear = wear,
        special = special,
        patternIndex = patternIndex,
        keychainNames = keychainNames
    )

    /** ID чата с поддержкой (должен совпадать с [ru.kotlix.skinshowcase.message.chats.ChatsListViewModel.SUPPORT_CHAT_ID]). */
    const val SUPPORT_CHAT_ID = "support"

    private val mutableChats: MutableList<ChatDto> = mutableListOf(
        ChatDto(SUPPORT_CHAT_ID, "Поддержка", "Здравствуйте! Опишите вашу проблему или вопрос — мы постараемся помочь.", "2025-03-14T10:00:00Z", null),
        ChatDto("76561198012345601", "Trader_AWP", "Привет! Рад знакомству.", "2025-03-14T12:30:00Z", null),
        ChatDto("76561198123456702", "SteamUser_Pro", "Добрый день!", "2025-03-13T18:00:00Z", null),
        ChatDto("76561198234567803", "HappyTrader", "Здравствуйте!", "2025-03-12T09:15:00Z", null)
    )

    fun getChats(): List<ChatDto> = mutableChats.toList()

    private val messagesByChat: MutableMap<String, MutableList<MessageDto>> = mutableMapOf(
        SUPPORT_CHAT_ID to mutableListOf(
            MessageDto(
                id = "support-welcome",
                text = "Здравствуйте! Вы обратились в поддержку SkinShowcase. Мы на связи и готовы помочь с любыми вопросами: по сделкам, обменам, работе приложения или аккаунта. Опишите вашу проблему или вопрос — ответим в ближайшее время.",
                isOutgoing = false,
                timeMillis = System.currentTimeMillis() - 86_400_000
            )
        ),
        "76561198012345601" to mutableListOf(
            MessageDto("msg-1-1", "Привет! Рад знакомству.", false, 1710412200000L),
            MessageDto("msg-1-2", "Привет! Как дела?", true, 1710412260000L),
            MessageDto("msg-1-3", "Добрый день!", false, 1710412320000L)
        ),
        "76561198123456702" to mutableListOf(
            MessageDto("msg-2-1", "Добрый день!", true, 1710324000000L),
            MessageDto("msg-2-2", "Здравствуйте!", false, 1710324060000L)
        ),
        "76561198234567803" to mutableListOf(
            MessageDto("msg-3-1", "Здравствуйте!", false, 1710234900000L),
            MessageDto("msg-3-2", "Привет! Хорошего дня!", true, 1710234960000L)
        )
    )

    fun getMessages(chatId: String): List<MessageDto> =
        messagesByChat[chatId]?.toList() ?: emptyList()

    fun addMessage(chatId: String, message: MessageDto) {
        messagesByChat.getOrPut(chatId) { mutableListOf() }.add(message)
        upsertChatInList(chatId, message.text)
    }

    private fun upsertChatInList(chatId: String, lastMessageText: String) {
        val nowIso = java.time.Instant.now().toString()
        val idx = mutableChats.indexOfFirst { it.counterpartySteamId == chatId }
        val nickname = knownSteamIdToNickname[chatId] ?: "Пользователь"
        val avatarUrl = knownSteamIdToAvatar[chatId]
        if (idx >= 0) {
            mutableChats[idx] = mutableChats[idx].copy(
                lastMessagePreview = lastMessageText,
                lastMessageAt = nowIso
            )
        } else {
            mutableChats.add(
                ChatDto(chatId, nickname, lastMessageText, nowIso, avatarUrl)
            )
        }
        sortChatsByLastMessage()
    }

    private fun sortChatsByLastMessage() {
        mutableChats.sortWith(compareBy<ChatDto> { it.counterpartySteamId != SUPPORT_CHAT_ID }.thenByDescending {
            it.lastMessageAt ?: ""
        })
    }

    /** Никнеймы и аватарки известных пользователей — для новых чатов, открытых из оффера. null = в UI иконка-человечек. */
    private val knownSteamIdToNickname: Map<String, String> = mapOf(
        "76561198012345601" to "Trader_AWP",
        "76561198123456702" to "SteamUser_Pro",
        "76561198234567803" to "HappyTrader",
        "76561198000000000" to "Пользователь"
    )
    private val knownSteamIdToAvatar: Map<String, String?> = mapOf(
        "76561198012345601" to null,
        "76561198123456702" to null,
        "76561198234567803" to null
    )

    val profileSteamNickname: String = "1116421"
    /** Аватар текущего пользователя: null — в UI показывается локальная картинка (ic_avatar_me). */
    val profileSteamAvatarUrl: String? = null
    val profileSteamId: String = "76561198123456789"

    val profileDeals: List<MockDealSummary> = listOf(
        MockDealSummary("deal-1", "Обмен: AK-47 Elite Build на AWP Safari Mesh", "SteamUser_123"),
        MockDealSummary("deal-2", "Покупка: P90 | Elite Build — 125 ₽", "Trader_Pro")
    )

    private val mutableOffersList = mutableListOf(
        MockOfferSummary("offer-1", "ak47-safari-mesh", "AK-47 | Safari Mesh", skinImageUrls["ak47-safari-mesh"]!!, 28.0),
        MockOfferSummary("offer-2", "awp-safari-mesh", "AWP | Safari Mesh", skinImageUrls["awp-safari-mesh"]!!, 480.0),
        MockOfferSummary("offer-3", "glock-18-ironwork", "Glock-18 | Ironwork", skinImageUrls["glock-18-ironwork"]!!, 65.0),
        MockOfferSummary("offer-4", "usp-s-lead-conduit", "USP-S | Lead Conduit", skinImageUrls["usp-s-lead-conduit"]!!, 95.0)
    )

    fun getOffers(): List<MockOfferSummary> = mutableOffersList.toList()

    /** Удаляет оффер по id. Возвращает true, если оффер был удалён. */
    fun removeOffer(id: String): Boolean {
        val removed = mutableOffersList.removeAll { it.id == id }
        return removed
    }

    /** Удаляет сообщение в чате по id. Возвращает true, если сообщение было удалено. */
    fun removeMessage(chatId: String, messageId: String): Boolean {
        val list = messagesByChat[chatId] ?: return false
        return list.removeAll { it.id == messageId }
    }

    /** Удаляет чат и все его сообщения. Возвращает true, если чат был удалён. */
    fun removeChat(chatId: String): Boolean {
        val removed = mutableChats.removeAll { it.counterpartySteamId == chatId }
        messagesByChat.remove(chatId)
        return removed
    }

    /** Продавцы по skinId (для чужого оффера): у части есть trade link, у части нет. */
    private val sellersBySkinId: Map<String, MockSellerInfo> = mapOf(
        "ak47-safari-mesh" to MockSellerInfo("Trader_AWP", "76561198012345601", "https://steamcommunity.com/tradeoffer/new/?partner=12345601&token=abc123"),
        "awp-safari-mesh" to MockSellerInfo("SteamUser_Pro", "76561198123456702", null),
        "glock-18-ironwork" to MockSellerInfo("HappyTrader", "76561198234567803", "https://steamcommunity.com/tradeoffer/new/?partner=23456703&token=xyz789")
    )

    fun getSellerForSkin(skinId: String): MockSellerInfo =
        sellersBySkinId[skinId] ?: MockSellerInfo("Пользователь", "76561198000000000", null)

    /** Добавляет оффер по skinId; возвращает id нового оффера или null, если скин не найден. */
    fun addOffer(skinId: String): String? {
        val skin = skins.find { it.id == skinId } ?: return null
        val newId = "offer-${System.currentTimeMillis()}"
        val offer = MockOfferSummary(
            id = newId,
            skinId = skin.id,
            skinName = skin.name,
            skinImageUrl = skin.imageUrl,
            priceRub = skin.price
        )
        mutableOffersList.add(offer)
        return newId
    }
}
