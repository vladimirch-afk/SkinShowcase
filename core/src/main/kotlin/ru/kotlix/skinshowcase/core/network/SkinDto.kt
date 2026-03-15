package ru.kotlix.skinshowcase.core.network

import com.google.gson.annotations.SerializedName

data class SkinDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("floatValue") val floatValue: Double? = null,
    @SerializedName("stickerNames") val stickerNames: List<String>? = null,
    @SerializedName("collection") val collection: String? = null,
    @SerializedName("rarity") val rarity: String? = null,
    @SerializedName("wear") val wear: String? = null,
    @SerializedName("special") val special: String? = null,
    @SerializedName("patternIndex") val patternIndex: Int? = null,
    @SerializedName("keychainNames") val keychainNames: List<String>? = null
)
