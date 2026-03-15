package ru.kotlix.skinshowcase.screens.skins

import ru.kotlix.skinshowcase.core.BaseViewModel
import ru.kotlix.skinshowcase.core.domain.Skin

class SkinsViewModel : BaseViewModel<SkinsUiState>() {

    override fun initialState(): SkinsUiState = SkinsUiState(skins = defaultSkins())

    private fun defaultSkins(): List<Skin> = listOf(
        Skin(id = "ak47-redline", name = "АК-47 | Красная линия", imageUrl = null, price = 40_000.0),
        Skin(id = "butterfly-gradient", name = "Butterfly Knife | Градиент", imageUrl = null, price = 100_000.0),
        Skin(id = "awp-lighting", name = "AWP | Молния", imageUrl = null, price = 55_000.0)
    )
}
