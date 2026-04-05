package ru.kotlix.skinshowcase.screens.skindetail

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
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
import ru.kotlix.skinshowcase.analytics.AppAnalytics
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.designsystem.components.DataErrorDialog
import ru.kotlix.skinshowcase.designsystem.format.formatSkinPriceUsdAmount
import ru.kotlix.skinshowcase.designsystem.theme.PriceGreen
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientEnd
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientStart
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.components.NetworkImage

private val CARD_SHAPE = RoundedCornerShape(12.dp)
private val IMAGE_HEIGHT = 200.dp
private val DETAIL_ROW_SPACING = 10.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinDetailScreen(
    skinId: String,
    isOwnOffer: Boolean = false,
    isCreatingOffer: Boolean = false,
    onBack: () -> Unit,
    onOfferCreated: () -> Unit = {},
    onOpenChatWithSeller: (String) -> Unit = {},
    viewModel: SkinDetailViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val skin = state.skin
    val context = LocalContext.current
    var showGoToSteamProfileDialog by remember { mutableStateOf(false) }
    var showNoTradeLinkWarning by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var reportDetails by remember { mutableStateOf("") }
    var reportSending by remember { mutableStateOf(false) }
    var reportDialogError by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(state.navigateToMyOffers) {
        if (state.navigateToMyOffers) {
            onOfferCreated()
            viewModel.clearNavigateToMyOffers()
        }
    }

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
                    message = state.errorMessage ?: stringResource(R.string.error_data_message),
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
            NetworkImage(
                url = skin?.imageUrl,
                contentDescription = skin?.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IMAGE_HEIGHT)
                    .clip(RoundedCornerShape(12.dp))
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
            skin?.itemDescription?.trim()?.takeIf { it.isNotEmpty() }?.let { desc ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.skin_detail_description),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                SkinDescriptionHtml(html = desc)
            }
            skin?.inspectLink?.trim()?.takeIf { it.isNotEmpty() }?.let { link ->
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.skin_detail_inspect))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (state.sellerSteamId != null) Modifier.clickable { showGoToSteamProfileDialog = true }
                        else Modifier
                    ),
                shape = CARD_SHAPE,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.skin_detail_seller),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.sellerNickname?.trim()?.takeIf { it.isNotEmpty() } ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.sellerSteamId ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!isOwnOffer && !state.isCreatingOffer && state.sellerSteamId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onOpenChatWithSeller(state.sellerSteamId!!) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.skin_detail_open_chat))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        reportReason = ""
                        reportDetails = ""
                        reportDialogError = null
                        showReportDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.skin_detail_report))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (state.isCreatingOffer) {
                Button(
                    onClick = { viewModel.createOffer() },
                    enabled = !state.isSubmittingOffer,
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
                    if (state.isSubmittingOffer) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.skin_detail_create_offer),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else if (!isOwnOffer) {
                Button(
                    onClick = {
                        val link = state.sellerTradeLink
                        if (!link.isNullOrBlank()) {
                            AppAnalytics.reportEvent("trade_link_open", mapOf("skin_id" to skinId))
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                        } else {
                            showNoTradeLinkWarning = true
                        }
                    },
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
        if (showReportDialog && state.sellerSteamId != null) {
            AlertDialog(
                onDismissRequest = {
                    if (!reportSending) showReportDialog = false
                },
                title = { Text(stringResource(R.string.skin_detail_report_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = reportReason,
                            onValueChange = {
                                reportReason = it
                                reportDialogError = null
                            },
                            label = { Text(stringResource(R.string.skin_detail_report_reason)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !reportSending
                        )
                        OutlinedTextField(
                            value = reportDetails,
                            onValueChange = {
                                reportDetails = it
                                reportDialogError = null
                            },
                            label = { Text(stringResource(R.string.skin_detail_report_details)) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !reportSending,
                            minLines = 2,
                            maxLines = 4
                        )
                        reportDialogError?.let { err ->
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            reportSending = true
                            reportDialogError = null
                            viewModel.reportSeller(
                                reason = reportReason,
                                details = reportDetails
                            ) { err ->
                                reportSending = false
                                if (err == null) {
                                    showReportDialog = false
                                } else {
                                    reportDialogError = err
                                }
                            }
                        },
                        enabled = !reportSending && reportReason.isNotBlank()
                    ) {
                        if (reportSending) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(stringResource(R.string.skin_detail_report_send))
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showReportDialog = false },
                        enabled = !reportSending
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }
        if (showGoToSteamProfileDialog && state.sellerSteamId != null) {
            AlertDialog(
                onDismissRequest = { showGoToSteamProfileDialog = false },
                title = { Text(stringResource(R.string.skin_detail_go_to_steam_title)) },
                text = { Text(stringResource(R.string.skin_detail_go_to_steam_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            state.sellerSteamId?.let { steamId ->
                                AppAnalytics.reportEvent("steam_profile_open", mapOf("steam_id" to steamId))
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://steamcommunity.com/profiles/$steamId"))
                                )
                            }
                            showGoToSteamProfileDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.skin_detail_go_to_steam_open))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoToSteamProfileDialog = false }) {
                        Text(stringResource(R.string.offers_delete_confirm_cancel))
                    }
                }
            )
        }
        state.offerCreateError?.let { err ->
            AlertDialog(
                onDismissRequest = viewModel::clearOfferCreateError,
                title = { Text(stringResource(R.string.skin_detail_offer_create_failed_title)) },
                text = { Text(err) },
                confirmButton = {
                    TextButton(onClick = viewModel::clearOfferCreateError) {
                        Text(stringResource(R.string.error_dialog_ok))
                    }
                }
            )
        }
        if (showNoTradeLinkWarning) {
            AlertDialog(
                onDismissRequest = { showNoTradeLinkWarning = false },
                title = { Text(stringResource(R.string.skin_detail_no_trade_link_title)) },
                text = { Text(stringResource(R.string.skin_detail_no_trade_link_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            state.sellerSteamId?.let { steamId ->
                                AppAnalytics.reportEvent("steam_profile_open", mapOf("steam_id" to steamId))
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://steamcommunity.com/profiles/$steamId"))
                                )
                            }
                            showNoTradeLinkWarning = false
                        }
                    ) {
                        Text(stringResource(R.string.skin_detail_go_to_profile))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoTradeLinkWarning = false }) {
                        Text(stringResource(R.string.error_dialog_ok))
                    }
                }
            )
        }
    }
}

@Composable
private fun SkinDetailsGrid(
    skin: Skin?,
    modifier: Modifier = Modifier
) {
    val noValue = stringResource(R.string.skin_detail_no_value)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            skin == null -> SkinDetailsWeaponColumns(skin = null, noValue = noValue)
            skin.isContainerLikeItem -> SkinDetailsContainerBlock(skin = skin, noValue = noValue)
            else -> {
                SkinDetailsWeaponColumns(skin = skin, noValue = noValue)
                skin.steamItemType?.takeIf { it.isNotBlank() }?.let { typeLine ->
                    DetailRow(label = stringResource(R.string.skin_detail_type), value = typeLine)
                }
                skin.marketHashName?.takeIf { it.isNotBlank() && !it.equals(skin.name, ignoreCase = true) }?.let { mh ->
                    DetailRow(label = stringResource(R.string.skin_detail_market_hash), value = mh)
                }
            }
        }
        skin?.extraInfoLines.orEmpty().forEach { line ->
            DetailRow(label = line.label, value = line.value)
        }
    }
}

@Composable
private fun SkinDetailsContainerBlock(skin: Skin, noValue: String) {
    Column(verticalArrangement = Arrangement.spacedBy(DETAIL_ROW_SPACING)) {
        skin.steamItemType?.takeIf { it.isNotBlank() }?.let { typeLine ->
            DetailRow(label = stringResource(R.string.skin_detail_type), value = typeLine)
        }
        DetailRow(
            label = stringResource(R.string.skin_detail_collection),
            value = skin.collection?.takeIf { it.isNotBlank() } ?: noValue
        )
        skin.marketHashName?.takeIf { it.isNotBlank() && !it.equals(skin.name, ignoreCase = true) }?.let { mh ->
            DetailRow(label = stringResource(R.string.skin_detail_market_hash), value = mh)
        }
        DetailRow(
            label = stringResource(R.string.skin_detail_price),
            value = skin.price?.let { formatSkinPriceUsdAmount(it) } ?: noValue
        )
        skin.amount?.takeIf { it > 1 }?.let { amt ->
            DetailRow(label = stringResource(R.string.skin_detail_amount), value = amt.toString())
        }
    }
}

@Composable
private fun SkinDetailsWeaponColumns(skin: Skin?, noValue: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DETAIL_ROW_SPACING)
        ) {
            DetailRow(
                label = stringResource(R.string.skin_detail_float),
                value = skin?.floatValue?.let { formatFloatForDisplay(it) } ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_stickers),
                value = formatList(skin?.stickerNames) ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_collection),
                value = skin?.collection?.takeIf { it.isNotBlank() } ?: noValue
            )
            DetailRow(
                label = stringResource(R.string.skin_detail_price),
                value = skin?.price?.let { formatSkinPriceUsdAmount(it) } ?: noValue
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
                label = stringResource(R.string.skin_detail_special),
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
private fun SkinDescriptionHtml(
    html: String,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary
    val body = MaterialTheme.typography.bodyMedium
    val rawSp = body.fontSize.value
    val fontSizeSp = if (rawSp in 8f..40f) rawSp else 14f
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                includeFontPadding = false
            }
        },
        update = { textView ->
            textView.setTextColor(textColor.toArgb())
            textView.setLinkTextColor(linkColor.toArgb())
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            val normalized = html.replace("\n", "<br/>")
            textView.text = HtmlCompat.fromHtml(normalized, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
    )
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

private fun formatFloatForDisplay(value: Double): String {
    val s = String.format(java.util.Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
    return if (s.isEmpty()) value.toString() else s
}

@Preview(showBackground = true)
@Composable
private fun SkinDetailScreenPreview() {
    SkinShowcaseTheme {
        SkinDetailScreen(skinId = "sample-id", onBack = { })
    }
}
