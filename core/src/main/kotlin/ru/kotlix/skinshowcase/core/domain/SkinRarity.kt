package ru.kotlix.skinshowcase.core.domain

/**
 * Редкость скина (CS2).
 */
enum class SkinRarity(val displayName: String) {
    CONSUMER_GRADE("Consumer Grade"),
    INDUSTRIAL_GRADE("Industrial Grade"),
    MIL_SPEC("Mil-Spec"),
    RESTRICTED("Restricted"),
    CLASSIFIED("Classified"),
    COVERT("Covert"),
    CONTRABAND("Contraband"),
    EXTRAORDINARY("Extraordinary")
}
