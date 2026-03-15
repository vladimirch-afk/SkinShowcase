package ru.kotlix.skinshowcase.onboarding

import ru.kotlix.skinshowcase.core.BaseViewModel

class OnboardingViewModel : BaseViewModel<OnboardingUiState>() {

    override fun initialState(): OnboardingUiState = OnboardingUiState()
}
