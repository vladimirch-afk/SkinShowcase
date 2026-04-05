package ru.kotlix.skinshowcase.screens.createoffer

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilterApplicator
import ru.kotlix.skinshowcase.designsystem.components.DataErrorDialog
import ru.kotlix.skinshowcase.screens.home.AppliedFiltersRow
import ru.kotlix.skinshowcase.screens.home.HomeCreateOfferButton
import ru.kotlix.skinshowcase.screens.home.HomeFilterSheet
import ru.kotlix.skinshowcase.screens.home.HomeTopBar
import ru.kotlix.skinshowcase.screens.home.filterSkinsByQuery
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.designsystem.format.formatSkinPriceUsd
import ru.kotlix.skinshowcase.designsystem.theme.PriceGreen
import ru.kotlix.skinshowcase.components.NetworkImage

private val CARD_SHAPE = RoundedCornerShape(12.dp)
private const val GRID_COLUMNS = 2
private val GRID_SPACING = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferSelectSkinScreen(
    onBack: () -> Unit,
    onSkinClick: (skinId: String, inventoryAssetId: String?) -> Unit,
    onConfirmCreateOffer: () -> Unit = {},
    viewModel: CreateOfferSelectSkinViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadSkins()
    }

    LaunchedEffect(state.selectionHint) {
        val msg = state.selectionHint ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = msg)
        viewModel.clearSelectionHint()
    }

    if (state.filterSheetVisible) {
        HomeFilterSheet(
            currentFilter = state.filter,
            onDismiss = viewModel::dismissFilterSheet,
            onApply = viewModel::applyFilter
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.create_offer_select_skin_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
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

        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
        } else if (state.isLoading && state.skins.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else if (state.skins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.create_offer_inventory_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val filteredSkins = remember(
                state.skins,
                state.filter,
                state.searchQuery
            ) {
                val byFilter = SkinFilterApplicator.apply(state.skins, state.filter)
                filterSkinsByQuery(byFilter, state.searchQuery)
            }
            val selectionChangedSinceLoad = remember(
                state.baselineTradeSelectionItems,
                state.tradeSelectionItems
            ) {
                !ProfileDataProvider.tradeSelectionSetsEqual(
                    state.baselineTradeSelectionItems,
                    state.tradeSelectionItems
                )
            }
            val showCreateOfferBar = selectionChangedSinceLoad
            Column(modifier = Modifier.fillMaxSize()) {
                HomeTopBar(
                    searchQuery = state.searchQuery,
                    onSearchChange = viewModel::updateSearch,
                    onFilterClick = viewModel::openFilterSheet
                )
                AppliedFiltersRow(
                    filter = state.filter,
                    onFilterClick = viewModel::openFilterSheet
                )
                if (filteredSkins.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.create_offer_no_filter_matches),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val rows = remember(filteredSkins, state.groupByName) {
                        filteredSkins.toInventoryGridRows(state.groupByName)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.create_offer_group_by_name),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = state.groupByName,
                            onCheckedChange = viewModel::setGroupByName,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GRID_COLUMNS),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
                        verticalArrangement = Arrangement.spacedBy(GRID_SPACING)
                    ) {
                        itemsIndexed(
                            items = rows,
                            key = { index, row ->
                                row.stableGridKey(index)
                            }
                        ) { _, row ->
                            val inOffer = state.tradeSelectionItems.any {
                                ProfileDataProvider.skinMatchesTradeSelectionItem(row.skin, it)
                            }
                            MySkinGridCard(
                                skin = row.skin,
                                stackLabel = row.stackLabel,
                                isInOffer = inOffer,
                                isToggling = state.togglingSkinKey == skinToggleKey(row.skin),
                                onOpenDetail = { onSkinClick(row.skin.id, row.skin.inventoryAssetId) },
                                onToggleSelection = { viewModel.toggleOfferSelection(row.skin) }
                            )
                        }
                    }
                }
                if (showCreateOfferBar) {
                    HomeCreateOfferButton(
                        onClick = onConfirmCreateOffer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = if (state.skins.isNotEmpty() && state.errorMessage == null) {
                        val changed = !ProfileDataProvider.tradeSelectionSetsEqual(
                            state.baselineTradeSelectionItems,
                            state.tradeSelectionItems
                        )
                        if (changed) 88.dp else 0.dp
                    } else {
                        0.dp
                    }
                )
        )
    }
}

private data class InventoryGridRow(
    val skin: Skin,
    val stackLabel: String?
)

private fun List<Skin>.toInventoryGridRows(groupByName: Boolean): List<InventoryGridRow> {
    if (!groupByName) {
        return map { InventoryGridRow(it, null) }
    }
    return groupBy { it.name }
        .values
        .map { list ->
            InventoryGridRow(skin = list.first(), stackLabel = "x${list.size}")
        }
        .sortedBy { it.skin.name.lowercase() }
}

private fun InventoryGridRow.stableGridKey(fallbackIndex: Int): String {
    val asset = skin.inventoryAssetId?.takeIf { it.isNotBlank() }
    if (stackLabel != null) {
        return "grp:${skin.name}"
    }
    return asset ?: "${skin.id}_$fallbackIndex"
}

@Composable
private fun MySkinGridCard(
    skin: Skin,
    isInOffer: Boolean,
    isToggling: Boolean,
    onOpenDetail: () -> Unit,
    onToggleSelection: () -> Unit,
    stackLabel: String? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        shape = CARD_SHAPE,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onOpenDetail)
            ) {
                NetworkImage(
                    url = skin.imageUrl,
                    contentDescription = skin.name,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = skin.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatSkinPriceUsd(skin.price),
                        style = MaterialTheme.typography.labelMedium,
                        color = PriceGreen
                    )
                }
                stackLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    )
                }
            }
            OfferSelectionBadge(
                selected = isInOffer,
                loading = isToggling,
                onToggle = onToggleSelection,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun OfferSelectionBadge(
    selected: Boolean,
    loading: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outline = MaterialTheme.colorScheme.outline
    val fill = PriceGreen
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = if (selected) fill else outline,
                shape = CircleShape
            )
            .background(
                color = if (selected) fill else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onToggle, enabled = !loading),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = if (selected) Color.White else MaterialTheme.colorScheme.primary
            )
            selected -> Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.create_offer_selection_toggle_cd),
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
