package ru.kotlix.skinshowcase.core.domain

/**
 * Применяет [SkinFilter] к списку скинов.
 * Критерии с пустыми значениями не учитываются.
 */
object SkinFilterApplicator {

    fun apply(skins: List<Skin>, filter: SkinFilter): List<Skin> {
        if (filter.isEmpty()) return skins
        return skins.filter { skin -> matches(skin, filter) }
    }

    private fun matches(skin: Skin, filter: SkinFilter): Boolean {
        if (!matchesPrice(skin, filter)) return false
        if (!matchesFloat(skin, filter)) return false
        if (!matchesNameContains(skin, filter)) return false
        if (!matchesNameExcludes(skin, filter)) return false
        if (!matchesSpecial(skin, filter)) return false
        if (!matchesPattern(skin, filter)) return false
        if (!matchesStickers(skin, filter)) return false
        if (!matchesKeychain(skin, filter)) return false
        if (!matchesRarity(skin, filter)) return false
        if (!matchesCollection(skin, filter)) return false
        if (!matchesWear(skin, filter)) return false
        return true
    }

    private fun matchesPrice(skin: Skin, filter: SkinFilter): Boolean {
        val price = skin.price ?: return filter.priceMin == null && filter.priceMax == null
        filter.priceMin?.let { if (price < it) return false }
        filter.priceMax?.let { if (price > it) return false }
        return true
    }

    private fun matchesFloat(skin: Skin, filter: SkinFilter): Boolean {
        val float = skin.floatValue ?: return filter.floatMin == null && filter.floatMax == null
        filter.floatMin?.let { if (float < it) return false }
        filter.floatMax?.let { if (float > it) return false }
        return true
    }

    private fun matchesNameContains(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.nameContains.isBlank()) return true
        return skin.name.contains(filter.nameContains, ignoreCase = true)
    }

    private fun matchesNameExcludes(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.nameExcludes.isEmpty()) return true
        val lower = skin.name.lowercase()
        return filter.nameExcludes.none { lower.contains(it.lowercase()) }
    }

    private fun matchesSpecial(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.specials.isEmpty()) return true
        val s = skin.special ?: return false
        return s in filter.specials
    }

    private fun matchesPattern(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.patternIndices.isEmpty()) return true
        val idx = skin.patternIndex ?: return false
        return idx in filter.patternIndices
    }

    private fun matchesStickers(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.requiredStickerNames.isEmpty()) return true
        val skinNames = skin.stickerNames.ifEmpty { skin.stickerIds }
        return filter.requiredStickerNames.all { name ->
            skinNames.any { it.equals(name, ignoreCase = true) }
        }
    }

    private fun matchesKeychain(skin: Skin, filter: SkinFilter): Boolean {
        when (filter.requireKeychain) {
            null -> return true
            false -> return !skin.hasKeychain
            true -> {
                if (filter.keychainNames.isEmpty()) return skin.hasKeychain
                val skinKeys = skin.keychainNames
                return skin.hasKeychain && filter.keychainNames.any { name ->
                    skinKeys.any { it.equals(name, ignoreCase = true) }
                }
            }
        }
    }

    private fun matchesRarity(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.rarities.isEmpty()) return true
        val r = skin.rarity ?: return false
        return r in filter.rarities
    }

    private fun matchesCollection(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.collections.isEmpty()) return true
        val c = skin.collection ?: return false
        return filter.collections.any { c.contains(it, ignoreCase = true) }
    }

    private fun matchesWear(skin: Skin, filter: SkinFilter): Boolean {
        if (filter.wears.isEmpty()) return true
        val w = skin.wear ?: return false
        return w in filter.wears
    }
}
