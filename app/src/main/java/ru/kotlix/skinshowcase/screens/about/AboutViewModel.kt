package ru.kotlix.skinshowcase.screens.about

import ru.kotlix.skinshowcase.core.BaseViewModel

class AboutViewModel : BaseViewModel<AboutUiState>() {

    override fun initialState(): AboutUiState = AboutUiState(appVersion = "1.0")
}
