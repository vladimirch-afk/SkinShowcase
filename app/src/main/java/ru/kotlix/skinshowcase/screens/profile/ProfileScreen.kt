package ru.kotlix.skinshowcase.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import ru.kotlix.skinshowcase.designsystem.theme.GoldAccent
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientEnd
import ru.kotlix.skinshowcase.designsystem.theme.PurpleBlueGradientStart
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme

private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val CARD_PADDING = 16.dp
private val AVATAR_SIZE = 64.dp
private val SKIN_THUMB_SIZE = 48.dp

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
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
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPrivacy()
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
        ProfileHeaderCard(
            steamNickname = state.steamNickname,
            steamAvatarUrl = state.steamAvatarUrl
        )
        Spacer(modifier = Modifier.height(16.dp))

        ActiveOffersCard(
            offers = state.activeOffers,
            onViewAll = onViewAllOffers,
            onCreateOffer = onCreateOffer
        )
        Spacer(modifier = Modifier.height(12.dp))

        DealHistoryCard(
            deals = state.dealHistory,
            hasActiveOffers = state.hasActiveOffers,
            onViewFullHistory = onViewFullHistory,
            onCreateOffer = onCreateOffer
        )
        Spacer(modifier = Modifier.height(12.dp))

        FavoritesCard(onClick = onNavigateToFavorites)
        Spacer(modifier = Modifier.height(12.dp))

        DocumentsCard(onDocumentClick = onDocumentClick)
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
    }
    }
}

@Composable
private fun ProfileHeaderCard(
    steamNickname: String,
    steamAvatarUrl: String?,
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
                Box(
                    modifier = Modifier
                        .size(AVATAR_SIZE)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (steamAvatarUrl != null) {
                        // Coil/Glide image here when integrated
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.profile_your_profile),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = steamNickname.ifEmpty { "—" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
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
        Box(
            modifier = Modifier
                .size(SKIN_THUMB_SIZE)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
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
                    text = "${offer.priceRub.toInt()} ₽",
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
private fun DealHistoryCard(
    deals: List<DealSummary>,
    hasActiveOffers: Boolean,
    onViewFullHistory: () -> Unit,
    onCreateOffer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasDeals = deals.isNotEmpty()
    ProfileSectionCard(
        title = stringResource(R.string.profile_trade_history),
        modifier = modifier
    ) {
        when {
            hasDeals && deals.size >= 2 -> {
                ProfilePeekRow(
                    gradientColor = MaterialTheme.colorScheme.surface,
                    item1 = {
                        DealRowContent(
                            deal = deals[0],
                            modifier = Modifier.clickable(onClick = onViewFullHistory)
                        )
                    },
                    item2 = {
                        DealRowContent(
                            deal = deals[1],
                            modifier = Modifier.clickable(onClick = onViewFullHistory)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_view_full_history),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewFullHistory)
                )
            }
            hasDeals && deals.size == 1 -> {
                DealRowContent(
                    deal = deals.first(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewFullHistory)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_view_full_history),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewFullHistory)
                )
            }
            hasActiveOffers -> {
                Text(
                    text = stringResource(R.string.profile_no_deals_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                Text(
                    text = stringResource(R.string.profile_no_offers_invite),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_create_offer_cta),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onCreateOffer)
                )
            }
        }
    }
}

@Composable
private fun DealRowContent(
    deal: DealSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deal.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            deal.counterpartName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    onDocumentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileSectionCard(
        title = stringResource(R.string.profile_documents),
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.profile_user_agreement),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDocumentClick("agreement") }
            )
            Text(
                text = stringResource(R.string.profile_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDocumentClick("instructions") }
            )
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
