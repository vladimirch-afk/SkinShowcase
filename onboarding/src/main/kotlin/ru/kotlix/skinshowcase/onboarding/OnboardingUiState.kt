package ru.kotlix.skinshowcase.onboarding

data class OnboardingUiState(
    val login: String = "",
    val password: String = "",
    val loginError: String? = null,
    val isLoading: Boolean = false
)
