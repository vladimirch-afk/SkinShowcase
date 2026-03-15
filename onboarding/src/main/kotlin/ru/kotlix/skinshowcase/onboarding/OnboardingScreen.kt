package ru.kotlix.skinshowcase.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.onboarding.R

@Composable
fun OnboardingScreen(
    onLoginSuccess: () -> Unit = {},
    onSteamLoginClick: () -> Unit = {},
    onLoginAttempt: (method: String) -> Unit = {},
    viewModel: OnboardingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = state.login,
            onValueChange = viewModel::onLoginChange,
            label = { Text(stringResource(R.string.onboarding_login_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.loginError != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text(stringResource(R.string.onboarding_password_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = state.loginError != null
        )

        state.loginError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onLoginAttempt("form")
                viewModel.onLoginClick(onSuccess = onLoginSuccess)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(stringResource(R.string.onboarding_login_button))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSteamLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(stringResource(R.string.onboarding_steam_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    SkinShowcaseTheme {
        OnboardingScreen()
    }
}
