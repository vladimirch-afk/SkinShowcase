package ru.kotlix.skinshowcase.screens.home

import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.domain.SkinFilterApplicator

private const val PRIMARY_SLOT_COUNT = 5

/** Одна строка ленты обменов: до [PRIMARY_SLOT_COUNT] предметов в полосе и остаток в мини-карусели. */
data class HomeTradeFeedRow(
    val ownerSteamId: String,
    val primarySkins: List<Skin>,
    val carouselSkins: List<Skin>
)

internal fun filterSkinsByQuery(skins: List<Skin>, query: String): List<Skin> {
    if (query.isBlank()) return skins
    val lower = query.lowercase()
    return skins.filter { it.name.lowercase().contains(lower) }
}

/** Скин попадает в основную полосу (учитываются и поиск по слову, и фильтр листа). */
private fun skinMatchesSearchAndFilter(skin: Skin, searchQuery: String, filter: SkinFilter): Boolean {
    if (searchQuery.isNotBlank()) {
        val lower = searchQuery.lowercase()
        if (!skin.name.lowercase().contains(lower)) return false
    }
    return SkinFilterApplicator.matchesFilter(skin, filter)
}

internal fun sortSkins(skins: List<Skin>, option: SortOption): List<Skin> {
    if (option == SortOption.DEFAULT) return skins
    return when (option) {
        SortOption.DEFAULT -> skins
        SortOption.PRICE_ASC -> skins.sortedBy { it.price ?: Double.MAX_VALUE }
        SortOption.PRICE_DESC -> skins.sortedByDescending { it.price ?: Double.MIN_VALUE }
        SortOption.FLOAT_ASC -> skins.sortedBy { it.floatValue ?: Double.MAX_VALUE }
        SortOption.FLOAT_DESC -> skins.sortedByDescending { it.floatValue ?: Double.MIN_VALUE }
        SortOption.RARITY_ASC -> skins.sortedBy { it.rarity?.ordinal ?: Int.MAX_VALUE }
        SortOption.RARITY_DESC -> skins.sortedByDescending { it.rarity?.ordinal ?: Int.MIN_VALUE }
    }
}

/**
 * [orderedSkins] — плоский список в порядке выдачи фида (порядок селекций и позиций внутри).
 */
internal fun buildHomeTradeFeedRows(
    orderedSkins: List<Skin>,
    searchQuery: String,
    filter: SkinFilter,
    sortOption: SortOption
): List<HomeTradeFeedRow> {
    val ownerOrder = ArrayList<String>()
    val byOwner = linkedMapOf<String, MutableList<Skin>>()
    for (skin in orderedSkins) {
        val owner = skin.offerOwnerSteamId ?: continue
        if (!byOwner.containsKey(owner)) ownerOrder.add(owner)
        byOwner.getOrPut(owner) { mutableListOf() }.add(skin)
    }
    val matchingByOwner = linkedMapOf<String, List<Skin>>()
    for (owner in ownerOrder) {
        val all = byOwner[owner] ?: continue
        val matching = all.filter { skinMatchesSearchAndFilter(it, searchQuery, filter) }
        if (matching.isNotEmpty()) matchingByOwner[owner] = matching
    }
    val sortedOwners = sortOwnersForFeed(matchingByOwner, sortOption, ownerOrder)
    val rows = ArrayList<HomeTradeFeedRow>()
    for (owner in sortedOwners) {
        val all = byOwner[owner] ?: continue
        val matching = matchingByOwner[owner] ?: continue
        val primary = matching.take(PRIMARY_SLOT_COUNT)
        val restMatching = matching.drop(PRIMARY_SLOT_COUNT)
        val notInStripCriteria = all.filter { !skinMatchesSearchAndFilter(it, searchQuery, filter) }
        val carousel = restMatching + notInStripCriteria
        rows.add(HomeTradeFeedRow(ownerSteamId = owner, primarySkins = primary, carouselSkins = carousel))
    }
    return rows
}

internal fun HomeTradeFeedRow.ownerHasAnyFavorite(): Boolean =
    primarySkins.any { it.isFavorite } || carouselSkins.any { it.isFavorite }

private fun sortOwnersForFeed(
    matchingByOwner: Map<String, List<Skin>>,
    sortOption: SortOption,
    feedOwnerOrder: List<String>
): List<String> {
    val candidates = feedOwnerOrder.filter { matchingByOwner.containsKey(it) }
    if (sortOption == SortOption.DEFAULT) return candidates
    fun matchingList(owner: String): List<Skin> = matchingByOwner[owner].orEmpty()
    return when (sortOption) {
        SortOption.DEFAULT -> candidates
        SortOption.PRICE_ASC -> candidates.sortedBy { o ->
            matchingList(o).minOfOrNull { it.price ?: Double.MAX_VALUE } ?: Double.MAX_VALUE
        }
        SortOption.PRICE_DESC -> candidates.sortedByDescending { o ->
            matchingList(o).maxOfOrNull { it.price ?: Double.MIN_VALUE } ?: Double.MIN_VALUE
        }
        SortOption.FLOAT_ASC -> candidates.sortedBy { o ->
            matchingList(o).minOfOrNull { it.floatValue ?: Double.MAX_VALUE } ?: Double.MAX_VALUE
        }
        SortOption.FLOAT_DESC -> candidates.sortedByDescending { o ->
            matchingList(o).maxOfOrNull { it.floatValue ?: Double.MIN_VALUE } ?: Double.MIN_VALUE
        }
        SortOption.RARITY_ASC -> candidates.sortedBy { o ->
            matchingList(o).minOfOrNull { it.rarity?.ordinal ?: Int.MAX_VALUE } ?: Int.MAX_VALUE
        }
        SortOption.RARITY_DESC -> candidates.sortedByDescending { o ->
            matchingList(o).maxOfOrNull { it.rarity?.ordinal ?: Int.MIN_VALUE } ?: Int.MIN_VALUE
        }
    }
}
