package ru.kotlix.skinshowcase.core.network.inventory

import com.google.gson.annotations.SerializedName

/**
 * Ответ steam-gateway GET /api/v1/inventory/{steamId}/item (как в OpenAPI).
 */
data class InventoryItemDetailResponseDto(
    @SerializedName("steamId") val steamId: String? = null,
    @SerializedName("appId") val appId: Int? = null,
    @SerializedName("contextId") val contextId: Int? = null,
    @SerializedName("item") val item: InventoryItemPayloadDto? = null,
    @SerializedName("catalogPrice") val catalogPrice: ItemCatalogPricePayloadDto? = null
)

data class InventoryItemPayloadDto(
    @SerializedName(value = "assetId", alternate = ["asset_id"]) val assetId: String? = null,
    @SerializedName(value = "classId", alternate = ["class_id"]) val classId: String? = null,
    @SerializedName(value = "instanceId", alternate = ["instance_id"]) val instanceId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName(value = "marketHashName", alternate = ["market_hash_name"]) val marketHashName: String? = null,
    @SerializedName(value = "iconUrl", alternate = ["icon_url"]) val iconUrl: String? = null,
    @SerializedName(value = "floatValue", alternate = ["float_value"]) val floatValue: Double? = null,
    @SerializedName(value = "wearName", alternate = ["wear_name"]) val wearName: String? = null,
    @SerializedName(value = "paintSeed", alternate = ["paint_seed"]) val paintSeed: Int? = null,
    @SerializedName("pattern") val pattern: Int? = null,
    @SerializedName(value = "collectionName", alternate = ["collection_name"]) val collectionName: String? = null,
    @SerializedName(value = "fullItemName", alternate = ["full_item_name"]) val fullItemName: String? = null,
    @SerializedName(value = "rarityName", alternate = ["rarity_name"]) val rarityName: String? = null,
    @SerializedName(value = "qualityName", alternate = ["quality_name"]) val qualityName: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("amount") val amount: Int? = null,
    @SerializedName(value = "inspectLink", alternate = ["inspect_link"]) val inspectLink: String? = null,
    @SerializedName("stickers") val stickers: List<StickerPayloadDto>? = null,
    @SerializedName("extraAttributes") val extraAttributes: Map<String, String>? = null
)

data class StickerPayloadDto(
    @SerializedName("slot") val slot: Int? = null,
    @SerializedName(value = "stickerId", alternate = ["sticker_id"]) val stickerId: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("wear") val wear: Double? = null
)

data class ItemCatalogPricePayloadDto(
    @SerializedName(value = "itemId", alternate = ["item_id"]) val itemId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName(value = "minPriceUsd", alternate = ["min_price_usd"]) val minPriceUsd: Double? = null,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"]) val updatedAt: String? = null
)
