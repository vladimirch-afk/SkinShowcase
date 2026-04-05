package ru.kotlix.skinshowcase.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.drawOutline
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.core.network.auth.AvatarPresetDto
import ru.kotlix.skinshowcase.core.network.auth.AvatarUrls
import ru.kotlix.skinshowcase.core.network.auth.LegalDocumentSummaryDto
import ru.kotlix.skinshowcase.designsystem.format.formatSkinPriceUsd
import ru.kotlix.skinshowcase.designsystem.theme.GoldAccent
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientEnd
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientStart
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.components.NetworkImage

private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val CARD_PADDING = 16.dp
private val AVATAR_SIZE = 64.dp
private val SKIN_THUMB_SIZE = 48.dp

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToTradeLink: () -> Unit = {},
    onViewAllOffers: () -> Unit = {},
    onCreateOffer: () -> Unit = {},
    onViewFullHistory: () -> Unit = {},
    onDocumentClick: (String) -> Unit = {},
    onContactSupport: () -> Unit = {},
    onSupportProject: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDisplayName by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var avatarPickError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshProfile()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.clearRefreshing()
        }
    }
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) return@LaunchedEffect
        kotlinx.coroutines.delay(45_000)
        viewModel.clearRefreshing()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refreshProfile() },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
        state.refreshError?.let { err ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = err,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { viewModel.clearRefreshError() }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
        ProfileHeaderCard(
            displayName = state.steamNickname,
            steamAvatarUrl = state.steamAvatarUrl,
            steamId = state.steamId,
            onEditDisplayName = { showEditDisplayName = true },
            onChangeAvatar = { showAvatarPicker = true }
        )
        Spacer(modifier = Modifier.height(16.dp))

        TradeLinkCard(
            tradeLink = state.tradeLink,
            onClick = onNavigateToTradeLink
        )
        Spacer(modifier = Modifier.height(12.dp))

        ActiveOffersCard(
            offers = state.activeOffers,
            onViewAll = onViewAllOffers,
            onCreateOffer = onCreateOffer
        )
        Spacer(modifier = Modifier.height(12.dp))

        FavoritesCard(onClick = onNavigateToFavorites)
        Spacer(modifier = Modifier.height(12.dp))

        DocumentsCard(
            apiDocuments = state.legalDocumentsFromApi,
            onDocumentClick = onDocumentClick
        )
        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(
            showProfile = state.showProfile,
            showOffers = state.showOffers,
            onClick = onNavigateToSettings
        )
        Spacer(modifier = Modifier.height(12.dp))

        ContactSupportCard(onClick = onContactSupport)
        Spacer(modifier = Modifier.height(12.dp))

        SupportProjectButton(
            onClick = onSupportProject
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = stringResource(R.string.profile_logout),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (showEditDisplayName) {
            EditDisplayNameDialog(
                initialName = state.steamNickname,
                viewModel = viewModel,
                onDismiss = { showEditDisplayName = false }
            )
        }
        if (showAvatarPicker) {
            AvatarPickerDialog(
                presets = state.avatarPresets,
                selectedPresetId = state.avatarPresetId,
                avatarSource = state.avatarSource,
                steamProfileImageUrl = state.steamProfileImageUrl,
                viewModel = viewModel,
                errorMessage = avatarPickError,
                onErrorClear = { avatarPickError = null },
                onPickError = { avatarPickError = it },
                onDismiss = {
                    showAvatarPicker = false
                    avatarPickError = null
                }
            )
        }
    }
    }
}

@Composable
private fun AvatarPickerDialog(
    presets: List<AvatarPresetDto>,
    selectedPresetId: String?,
    avatarSource: String?,
    steamProfileImageUrl: String?,
    viewModel: ProfileViewModel,
    errorMessage: String?,
    onErrorClear: () -> Unit,
    onPickError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val src = avatarSource?.trim()?.uppercase(java.util.Locale.US)
    val steamSelected = src == "STEAM"
    val selectedPresetNum = selectedPresetId?.toIntOrNull() ?: AvatarUrls.DEFAULT_PRESET_ID.toInt()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_avatar_picker_title)) },
        text = {
            Column {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = listOf("steam_slot"),
                        key = { it }
                    ) {
                        val borderMod = if (steamSelected) {
                            Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        } else {
                            Modifier
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .then(borderMod)
                                .clickable {
                                    onErrorClear()
                                    viewModel.selectSteamAvatar(
                                        onSuccess = onDismiss,
                                        onError = onPickError
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!steamProfileImageUrl.isNullOrBlank()) {
                                NetworkImage(
                                    url = steamProfileImageUrl,
                                    contentDescription = stringResource(R.string.profile_avatar_steam),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.profile_avatar_steam),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    items(presets, key = { it.id ?: "" }) { preset ->
                        val id = preset.id?.trim().orEmpty()
                        if (id.isEmpty()) return@items
                        val itemNum = id.toIntOrNull() ?: return@items
                        val isSelected = !steamSelected && itemNum == selectedPresetNum
                        NetworkImage(
                            url = AvatarUrls.presetImageUrl(id),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable {
                                    onErrorClear()
                                    viewModel.selectAvatarPreset(
                                        id,
                                        onSuccess = onDismiss,
                                        onError = onPickError
                                    )
                                }
                        )
                    }
                }
                if (presets.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.error_data_title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun EditDisplayNameDialog(
    initialName: String,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    var draft by remember(initialName) { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_display_name_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = draft,
                    onValueChange = {
                        draft = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.profile_display_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.updateDisplayName(
                        draft,
                        onSuccess = onDismiss,
                        onError = { error = it }
                    )
                }
            ) {
                Text(stringResource(R.string.trade_link_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun TradeLinkCard(
    tradeLink: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLink = !tradeLink.isNullOrBlank()
    ProfileSectionCard(
        title = stringResource(R.string.profile_trade_link),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        if (!hasLink) {
            Text(
                text = stringResource(R.string.profile_trade_link_not_set),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = tradeLink,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    displayName: String,
    steamAvatarUrl: String?,
    steamId: String?,
    onEditDisplayName: () -> Unit,
    onChangeAvatar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PurpleBlueGradientStart.copy(alpha = 0.12f),
                            PurpleBlueGradientEnd.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(CARD_PADDING)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val avatarUrl = steamAvatarUrl?.takeIf { it.isNotBlank() }
                    ?: AvatarUrls.userAvatarUrl(null)
                NetworkImage(
                    url = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(AVATAR_SIZE)
                        .clip(CircleShape)
                        .clickable(onClick = onChangeAvatar)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = displayName.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.skin_detail_seller),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = steamId?.takeIf { it.isNotBlank() } ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.profile_display_name_edit),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onEditDisplayName)
                    )
                    Text(
                        text = stringResource(R.string.profile_avatar_change),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onChangeAvatar)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveOffersCard(
    offers: List<OfferSummary>,
    onViewAll: () -> Unit,
    onCreateOffer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasOffers = offers.isNotEmpty()
    ProfileSectionCard(
        title = stringResource(R.string.profile_active_offers),
        modifier = modifier
    ) {
        when {
            hasOffers && offers.size >= 2 -> {
                ProfilePeekRow(
                    gradientColor = MaterialTheme.colorScheme.surface,
                    item1 = {
                        OfferRowContent(
                            offer = offers[0],
                            modifier = Modifier.clickable(onClick = onViewAll)
                        )
                    },
                    item2 = {
                        OfferRowContent(
                            offer = offers[1],
                            modifier = Modifier.clickable(onClick = onViewAll)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.profile_view_all_offers),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewAll)
                )
            }
            hasOffers && offers.size == 1 -> {
                OfferRowContent(
                    offer = offers.first(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewAll)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.profile_view_all_offers),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewAll)
                )
            }
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCreateOffer)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = stringResource(R.string.profile_create_offer_cta),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OfferRowContent(
    offer: OfferSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NetworkImage(
            url = offer.skinImageUrl,
            contentDescription = offer.skinName,
            modifier = Modifier
                .size(SKIN_THUMB_SIZE)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = offer.skinName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (offer.priceRub != null) {
                Text(
                    text = formatSkinPriceUsd(offer.priceRub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val PEEK_ROW_HEIGHT = 56.dp

@Composable
private fun ProfilePeekRow(
    gradientColor: Color,
    modifier: Modifier = Modifier,
    item1: @Composable () -> Unit,
    item2: @Composable () -> Unit
) {
    val visibleHeight = PEEK_ROW_HEIGHT + (PEEK_ROW_HEIGHT.value / 2f).dp
    val totalHeight = PEEK_ROW_HEIGHT * 2
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(visibleHeight)
            .graphicsLayer { clip = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PEEK_ROW_HEIGHT)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                item1()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PEEK_ROW_HEIGHT)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                item2()
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height((PEEK_ROW_HEIGHT.value / 2f).dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            gradientColor
                        )
                    )
                )
        )
    }
}

@Composable
private fun FavoritesCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileSectionCard(
        title = stringResource(R.string.profile_favorites),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = stringResource(R.string.profile_view_favorites),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DocumentsCard(
    apiDocuments: List<LegalDocumentSummaryDto>,
    onDocumentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileSectionCard(
        title = stringResource(R.string.profile_documents),
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (apiDocuments.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_documents_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                apiDocuments.forEach { doc ->
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDocumentClick(doc.slug) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    showProfile: Boolean,
    showOffers: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileSectionCard(
        title = stringResource(R.string.screen_settings),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = stringResource(R.string.settings_privacy_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrivacyStatusChip(
                label = stringResource(R.string.settings_label_profile_short),
                visible = showProfile
            )
            PrivacyStatusChip(
                label = stringResource(R.string.settings_label_offers_short),
                visible = showOffers,
                isOffers = true
            )
        }
    }
}

@Composable
private fun PrivacyStatusChip(
    label: String,
    visible: Boolean,
    isOffers: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        isOffers && visible -> stringResource(R.string.settings_status_offers_visible)
        isOffers && !visible -> stringResource(R.string.settings_status_offers_hidden)
        visible -> stringResource(R.string.settings_status_visible)
        else -> stringResource(R.string.settings_status_hidden)
    }
    val backgroundColor = if (visible) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (visible) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ContactSupportCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileSectionCard(
        title = stringResource(R.string.profile_contact_support),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = stringResource(R.string.profile_contact_support),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SupportProjectButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "supportBorder")
    val anim = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "supportShine"
    ).value

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawWithContent {
                drawContent()
                val strokeWidth = 2.dp.toPx()
                val cornerRadius = 12.dp.toPx()
                val radius = CornerRadius(cornerRadius, cornerRadius)
                val roundRect = RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    topLeftCornerRadius = radius,
                    topRightCornerRadius = radius,
                    bottomRightCornerRadius = radius,
                    bottomLeftCornerRadius = radius
                )
                val sweepColors = (0 until 64).map { i ->
                    val t = (i / 64f + anim) % 1f
                    val alpha = when {
                        t < 0.04f -> 0f
                        t < 0.12f -> (t - 0.04f) / 0.08f
                        t < 0.22f -> 1f
                        t < 0.30f -> (0.30f - t) / 0.08f
                        else -> 0f
                    }
                    Color.White.copy(alpha = alpha)
                }
                drawOutline(
                    outline = Outline.Rounded(roundRect),
                    brush = Brush.sweepGradient(
                        colors = sweepColors,
                        center = Offset(size.width / 2f, size.height / 2f)
                    ),
                    style = Stroke(width = strokeWidth)
                )
            }
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent
            ),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.profile_support_project_cta),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.profile_support_project_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    SkinShowcaseTheme {
        ProfileScreen(
            onNavigateToSettings = {},
            onNavigateToAbout = {},
            onLogout = {}
        )
    }
}
