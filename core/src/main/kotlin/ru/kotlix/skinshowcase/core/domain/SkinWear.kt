package ru.kotlix.skinshowcase.core.domain

/**
 * Качество износа скина (CS2).
 */
enum class SkinWear(val displayName: String) {
    FN("Factory New"),
    MW("Minimal Wear"),
    FT("Field-Tested"),
    WW("Well-Worn"),
    BS("Battle-Scarred")
}
