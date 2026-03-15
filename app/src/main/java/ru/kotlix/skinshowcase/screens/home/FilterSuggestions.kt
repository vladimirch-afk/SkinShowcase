package ru.kotlix.skinshowcase.screens.home

/**
 * Список названий стикеров для автодополнения (примеры CS2).
 * В продакшене загружать с API.
 */
val STICKER_SUGGESTIONS: List<String> = listOf(
    "Howling Dawn",
    "Headhunter (Foil)",
    "iBUYPOWER (Holo)",
    "Titan (Holo)",
    "Vox Eminor (Holo)",
    "LGB eSports (Holo)",
    "Crown (Foil)",
    "Dragon Lore",
    "Faze (Holo)",
    "G2 (Holo)",
    "Natus Vincere (Holo)",
    "Cloud9 (Holo)",
    "Astralis (Holo)",
    "Virtus.Pro (Holo)",
    "Team Liquid (Holo)",
    "Sticker | Recoil",
    "Sticker | Headshot",
    "Sticker | Frag",
    "Sticker | Clutch"
)

/**
 * Список названий коллекций для автодополнения (примеры CS2).
 */
val COLLECTION_SUGGESTIONS: List<String> = listOf(
    "The Assault Collection",
    "The Arms Deal Collection",
    "The Alpha Collection",
    "The Huntsman Collection",
    "The Phoenix Collection",
    "The Vanguard Collection",
    "The Chroma Collection",
    "The Chroma 2 Collection",
    "The Falchion Collection",
    "The Shadow Collection",
    "The Gamma Collection",
    "The Gamma 2 Collection",
    "The Glove Collection",
    "Spectrum Collection",
    "Spectrum 2 Collection",
    "Clutch Collection",
    "CS20 Collection",
    "Danger Zone Collection",
    "Prisma Collection",
    "Prisma 2 Collection",
    "Shattered Web Collection",
    "Fracture Collection",
    "Dreams & Nightmares Collection",
    "Revolution Collection"
)

/**
 * Список названий брелков для автодополнения (примеры CS2).
 */
val KEYCHAIN_SUGGESTIONS: List<String> = listOf(
    "Bracelet",
    "Keychain",
    "Pins",
    "Patch",
    "Tag",
    "Charm"
)

fun filterSuggestions(query: String, options: List<String>): List<String> {
    if (query.isBlank()) return options
    val lower = query.lowercase()
    return options.filter { it.lowercase().contains(lower) }.take(10)
}
