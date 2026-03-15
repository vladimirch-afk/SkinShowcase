package ru.kotlix.skinshowcase.core.network

import com.google.gson.annotations.SerializedName

data class SkinDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("price") val price: Double? = null
)
