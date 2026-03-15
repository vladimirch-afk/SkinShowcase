package ru.kotlix.skinshowcase.screens.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import ru.kotlix.skinshowcase.core.domain.SkinRarity
import ru.kotlix.skinshowcase.core.domain.SkinWear

/** Бледный фон для невыбранного чипа: мягкий оттенок цвета. */
fun Color.paleVariant(): Color = lerp(Color.White, this, 0.52f)

/** Тёмный оттенок того же цвета для текста на бледном фоне — хороший контраст и читаемость. */
fun Color.darkLabelOnPale(): Color = lerp(this, Color(0xFF1A1A1A), 0.55f)

/**
 * Цвета редкости в стиле CS2.
 */
fun rarityColor(rarity: SkinRarity): Color = when (rarity) {
    SkinRarity.CONSUMER_GRADE -> Color(0xFFB0C3D9)
    SkinRarity.INDUSTRIAL_GRADE -> Color(0xFF5E98D9)
    SkinRarity.MIL_SPEC -> Color(0xFF4B69FF)
    SkinRarity.RESTRICTED -> Color(0xFF8847FF)
    SkinRarity.CLASSIFIED -> Color(0xFFD32CE6)
    SkinRarity.COVERT -> Color(0xFFEB4B4B)
    SkinRarity.CONTRABAND -> Color(0xFFE4AE33)
    SkinRarity.EXTRAORDINARY -> Color(0xFFEB4B4B)
}

/**
 * Цвета качества износа (CS2).
 */
fun wearColor(wear: SkinWear): Color = when (wear) {
    SkinWear.FN -> Color(0xFF4CAF50)
    SkinWear.MW -> Color(0xFF8BC34A)
    SkinWear.FT -> Color(0xFFFFC107)
    SkinWear.WW -> Color(0xFFFF9800)
    SkinWear.BS -> Color(0xFFF44336)
}
