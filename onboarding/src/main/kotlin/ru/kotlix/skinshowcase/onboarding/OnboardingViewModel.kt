package ru.kotlix.skinshowcase.onboarding

import ru.kotlix.skinshowcase.core.BaseViewModel

class OnboardingViewModel : BaseViewModel<OnboardingUiState>() {

    override fun initialState(): OnboardingUiState = OnboardingUiState()

    fun onLoginChange(value: String) {
        updateState { it.copy(login = value, loginError = null) }
    }

    fun onPasswordChange(value: String) {
        updateState { it.copy(password = value, loginError = null) }
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        val state = uiState.value
        if (state.login.isBlank()) {
            updateState { it.copy(loginError = "Введите логин") }
            return
        }
        if (state.password.isBlank()) {
            updateState { it.copy(loginError = "Введите пароль") }
            return
        }
        updateState { it.copy(isLoading = true, loginError = null) }
        launch {
            updateState { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}
