package ru.kotlix.skinshowcase.core.domain

/**
 * Тип скина в контексте CS2: Normal, Highlight, StatTrak™, Souvenir.
 */
enum class SkinSpecial(val displayName: String) {
    NORMAL("Normal"),
    HIGHLIGHT("Highlight"),
    STATTRAK("StatTrak™"),
    SOUVENIR("Souvenir")
}
