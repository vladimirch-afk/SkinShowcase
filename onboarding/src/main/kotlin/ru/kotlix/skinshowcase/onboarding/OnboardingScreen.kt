package ru.kotlix.skinshowcase.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientEnd
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientStart
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.onboarding.R

private val BUTTON_SHAPE = RoundedCornerShape(12.dp)
private val HORIZONTAL_PADDING = 32.dp
private val BUTTON_HEIGHT = 52.dp

@Composable
fun OnboardingScreen(
    onSteamLoginClick: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = HORIZONTAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onSteamLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .clip(BUTTON_SHAPE)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PurpleBlueGradientStart, PurpleBlueGradientEnd)
                    ),
                    shape = BUTTON_SHAPE
                ),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            contentPadding = ButtonDefaults.ContentPadding,
            enabled = !state.isLoading
        ) {
            Text(
                text = stringResource(R.string.onboarding_steam_button),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    SkinShowcaseTheme(darkTheme = true) {
        OnboardingScreen(onSteamLoginClick = {})
    }
}
