package ru.kotlix.skinshowcase.core.domain.mapper

import ru.kotlix.skinshowcase.core.database.entity.FavoriteSkinEntity
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.network.SkinDto

fun SkinDto.toDomain(isFavorite: Boolean = false): Skin =
    Skin(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        isFavorite = isFavorite
    )

fun FavoriteSkinEntity.toDomain(): Skin =
    Skin(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        isFavorite = true
    )

fun Skin.toFavoriteEntity(addedAtMillis: Long): FavoriteSkinEntity =
    FavoriteSkinEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        price = price,
        addedAtMillis = addedAtMillis
    )
