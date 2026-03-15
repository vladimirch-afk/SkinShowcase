package ru.kotlix.skinshowcase.screens.skindetail

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.designsystem.components.DataErrorDialog
import ru.kotlix.skinshowcase.designsystem.theme.PriceGreen
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientEnd
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientStart
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme

private val CARD_SHAPE = RoundedCornerShape(12.dp)
private val IMAGE_HEIGHT = 200.dp
private val DETAIL_ROW_SPACING = 10.dp
private val COLUMN_SPACING = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinDetailScreen(
    skinId: String,
    isOwnOffer: Boolean = false,
    onBack: () -> Unit,
    viewModel: SkinDetailViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val skin = state.skin

    Column(
        modifier = modifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.screen_skin_detail)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.error_data_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                val context = LocalContext.current
                DataErrorDialog(
                    title = stringResource(R.string.error_data_title),
                    message = stringResource(R.string.error_data_message),
                    okText = stringResource(R.string.error_dialog_ok),
                    settingsText = stringResource(R.string.error_dialog_settings),
                    onDismiss = viewModel::clearError,
                    onOpenSettings = {
                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    }
                )
            } else if (state.isLoading && state.skin == null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(Modifier.padding(24.dp))
                }
            } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IMAGE_HEIGHT)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = skin?.name ?: skinId,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            SkinDetailsGrid(skin = skin)
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CARD_SHAPE,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.skin_detail_seller),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@Seller",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (!isOwnOffer) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PurpleBlueGradientStart, PurpleBlueGradientEnd)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text(
                        text = stringResource(R.string.skin_detail_suggest_trade),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun SkinDetailsGrid(
    skin: Skin?,
    modifier: Modifier = Modifier
) {
    val noValue = stringResource(R.string.skin_detail_no_value)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DETAIL_ROW_SPACING)
        ) {
            DetailRow(
                label = stringResource(R.string.skin_detail_float),
                value = skin?.floatValue?.toString() ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_stickers),
                value = formatList(skin?.stickerNames) ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_collection),
                value = skin?.collection ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_price),
                value = skin?.price?.let { formatPriceRub(it) } ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_keychains),
                value = formatKeychains(skin) ?: noValue
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DETAIL_ROW_SPACING)
        ) {
            DetailRow(
                label = stringResource(R.string.skin_detail_rarity),
                value = skin?.rarity?.displayName ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_wear),
                value = skin?.wear?.displayName ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_type),
                value = skin?.special?.displayName ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_pattern),
                value = skin?.patternIndex?.toString() ?: noValue
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatList(list: List<String>?): String? {
    if (list == null || list.isEmpty()) return null
    return list.joinToString(", ")
}

private fun formatKeychains(skin: Skin?): String? {
    if (skin == null) return null
    if (skin.keychainNames.isNotEmpty()) return skin.keychainNames.joinToString(", ")
    return if (skin.hasKeychain) "Да" else null
}

private fun formatPriceRub(price: Double): String {
    val formatter = java.text.DecimalFormat("#,##0")
    formatter.decimalFormatSymbols = java.text.DecimalFormatSymbols(java.util.Locale("ru"))
    return "${formatter.format(price)} ₽"
}

@Preview(showBackground = true)
@Composable
private fun SkinDetailScreenPreview() {
    SkinShowcaseTheme {
        SkinDetailScreen(skinId = "sample-id", onBack = { })
    }
}
