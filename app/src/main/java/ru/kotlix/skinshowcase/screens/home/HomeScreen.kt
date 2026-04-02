package ru.kotlix.skinshowcase.screens.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.core.domain.Skin
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.domain.SkinFilterApplicator
import ru.kotlix.skinshowcase.core.domain.SkinRarity
import ru.kotlix.skinshowcase.core.domain.SkinSpecial
import ru.kotlix.skinshowcase.core.domain.SkinWear
import ru.kotlix.skinshowcase.designsystem.theme.PriceGreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.kotlix.skinshowcase.designsystem.components.DataErrorDialog
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.components.NetworkImage

private val CARD_SHAPE = RoundedCornerShape(12.dp)
private val CARD_BORDER_DP = 1.dp
private val IMAGE_PLACEHOLDER_SIZE = 72.dp
private val AVATAR_SIZE = 32.dp

@Composable
fun HomeScreen(
    onSkinClick: (skinId: String, offerOwnerSteamId: String?) -> Unit = { _, _ -> },
    onCreateOffer: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSkins()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.clearRefreshing() }
    }
    LaunchedEffect(state.skins) {
        viewModel.clearRefreshing()
    }
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) return@LaunchedEffect
        kotlinx.coroutines.delay(45_000)
        viewModel.clearRefreshing()
    }

    val byFilter = SkinFilterApplicator.apply(state.skins, state.filter)
    val filteredSkins = filterSkinsByQuery(byFilter, state.searchQuery)
    val sortedSkins = sortSkins(filteredSkins, state.sortOption)

    if (state.filterSheetVisible) {
        HomeFilterSheet(
            currentFilter = state.filter,
            onDismiss = viewModel::dismissFilterSheet,
            onApply = viewModel::applyFilter
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeTopBar(
            searchQuery = state.searchQuery,
            onSearchChange = viewModel::updateSearch,
            onFilterClick = viewModel::openFilterSheet
        )
        AppliedFiltersRow(
            filter = state.filter,
            onFilterClick = viewModel::openFilterSheet
        )
        SortByRow(
            sortOption = state.sortOption,
            onSortSelect = viewModel::setSortOption
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
                message = stringResource(R.string.error_data_message),
                okText = stringResource(R.string.error_dialog_ok),
                settingsText = stringResource(R.string.error_dialog_settings),
                onDismiss = viewModel::clearError,
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
            )
        } else {
        @OptIn(ExperimentalMaterial3Api::class)
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.loadSkins() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                if (state.isLoading && sortedSkins.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                }
                items(
                    items = sortedSkins,
                    key = { it.id }
                ) { skin ->
                    SkinListingCard(
                        skin = skin,
                        onClick = { onSkinClick(skin.id, skin.offerOwnerSteamId) },
                        onFavoriteClick = { viewModel.toggleFavorite(skin) },
                        showFavorite = skin.offerOwnerSteamId == null
                    )
                }
                // Чтобы список всегда скроллился и PullToRefresh срабатывал.
                item {
                    Spacer(modifier = Modifier.height(400.dp))
                }
            }
        }
        HomeCreateOfferButton(
            onClick = onCreateOffer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        }
    }
}

private data class AppliedFilterChip(val label: String, val tint: Color? = null)

private fun appliedFilterChips(filter: SkinFilter): List<AppliedFilterChip> {
    val list = mutableListOf<AppliedFilterChip>()
    if (filter.priceMin != null || filter.priceMax != null) {
        val s = when {
            filter.priceMin != null && filter.priceMax != null -> "Цена: ${filter.priceMin!!.toInt()} – ${filter.priceMax!!.toInt()}"
            filter.priceMin != null -> "Цена: от ${filter.priceMin!!.toInt()}"
            else -> "Цена: до ${filter.priceMax!!.toInt()}"
        }
        list.add(AppliedFilterChip(s))
    }
    if (filter.floatMin != null || filter.floatMax != null) {
        val s = when {
            filter.floatMin != null && filter.floatMax != null -> "Float: ${filter.floatMin} – ${filter.floatMax}"
            filter.floatMin != null -> "Float: от ${filter.floatMin}"
            else -> "Float: до ${filter.floatMax}"
        }
        list.add(AppliedFilterChip(s))
    }
    if (filter.nameContains.isNotBlank()) {
        list.add(AppliedFilterChip("Название: «${filter.nameContains}»"))
    }
    if (filter.nameExcludes.isNotEmpty()) {
        list.add(AppliedFilterChip("Не в названии: ${filter.nameExcludes.take(2).joinToString(", ")}${if (filter.nameExcludes.size > 2) "…" else ""}"))
    }
    filter.specials.forEach { list.add(AppliedFilterChip(it.displayName)) }
    if (filter.patternIndices.isNotEmpty()) {
        list.add(AppliedFilterChip("Паттерн: ${filter.patternIndices.take(5).joinToString(", ")}${if (filter.patternIndices.size > 5) "…" else ""}"))
    }
    filter.requiredStickerNames.forEach { list.add(AppliedFilterChip("Стикер: $it")) }
    when (filter.requireKeychain) {
        true -> {
            if (filter.keychainNames.isEmpty()) list.add(AppliedFilterChip("С брелком"))
            else filter.keychainNames.forEach { list.add(AppliedFilterChip("Брелок: $it")) }
        }
        false -> list.add(AppliedFilterChip("Без брелка"))
        null -> { }
    }
    filter.rarities.forEach { list.add(AppliedFilterChip(it.displayName, rarityColor(it))) }
    if (filter.collections.isNotEmpty()) {
        list.add(AppliedFilterChip("Коллекция: ${filter.collections.take(2).joinToString(", ")}${if (filter.collections.size > 2) "…" else ""}"))
    }
    filter.wears.forEach { list.add(AppliedFilterChip(it.displayName, wearColor(it))) }
    return list
}

@Composable
private fun AppliedFiltersRow(
    filter: SkinFilter,
    onFilterClick: () -> Unit
) {
    val chips = remember(filter) { appliedFilterChips(filter) }
    if (chips.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chips.forEach { item ->
            val chipColor = item.tint
            if (chipColor != null) {
                SuggestionChip(
                    onClick = onFilterClick,
                    label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = chipColor.paleVariant(),
                        labelColor = chipColor.darkLabelOnPale()
                    )
                )
            } else {
                SuggestionChip(
                    onClick = onFilterClick,
                    label = { Text(item.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortByRow(
    sortOption: SortOption,
    onSortSelect: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by androidx.compose.runtime.remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_sort_by) + ": " + sortOptionLabel(sortOption),
                    style = MaterialTheme.typography.labelLarge
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(sortOptionLabelRes(option))) },
                    onClick = {
                        onSortSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun sortOptionLabel(option: SortOption): String = when (option) {
    SortOption.DEFAULT -> "По умолчанию"
    SortOption.PRICE_ASC -> "Цена ↑"
    SortOption.PRICE_DESC -> "Цена ↓"
    SortOption.FLOAT_ASC -> "Float ↑"
    SortOption.FLOAT_DESC -> "Float ↓"
    SortOption.RARITY_ASC -> "Редкость ↑"
    SortOption.RARITY_DESC -> "Редкость ↓"
}

private fun sortOptionLabelRes(option: SortOption): Int = when (option) {
    SortOption.DEFAULT -> R.string.sort_default
    SortOption.PRICE_ASC -> R.string.sort_price_asc
    SortOption.PRICE_DESC -> R.string.sort_price_desc
    SortOption.FLOAT_ASC -> R.string.sort_float_asc
    SortOption.FLOAT_DESC -> R.string.sort_float_desc
    SortOption.RARITY_ASC -> R.string.sort_rarity_asc
    SortOption.RARITY_DESC -> R.string.sort_rarity_desc
}

@Composable
private fun HomeCreateOfferButton(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text = stringResource(R.string.home_create_offer),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun HomeTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.home_search_hint)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        IconButton(onClick = onFilterClick) {
            Icon(
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = stringResource(R.string.home_filter_description),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SkinListingCard(
    skin: Skin,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    showFavorite: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(CARD_BORDER_DP, MaterialTheme.colorScheme.outlineVariant, CARD_SHAPE),
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NetworkImage(
                url = skin.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(IMAGE_PLACEHOLDER_SIZE)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = skin.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatPriceRub(skin.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceGreen
                )
            }
            if (showFavorite) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (skin.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (skin.isFavorite) stringResource(R.string.favorites_remove) else stringResource(R.string.favorites_add),
                        tint = if (skin.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun filterSkinsByQuery(skins: List<Skin>, query: String): List<Skin> {
    if (query.isBlank()) return skins
    val lower = query.lowercase()
    return skins.filter { it.name.lowercase().contains(lower) }
}

private fun sortSkins(skins: List<Skin>, option: SortOption): List<Skin> {
    if (option == SortOption.DEFAULT) return skins
    return when (option) {
        SortOption.DEFAULT -> skins
        SortOption.PRICE_ASC -> skins.sortedBy { it.price ?: Double.MAX_VALUE }
        SortOption.PRICE_DESC -> skins.sortedByDescending { it.price ?: Double.MIN_VALUE }
        SortOption.FLOAT_ASC -> skins.sortedBy { it.floatValue ?: Double.MAX_VALUE }
        SortOption.FLOAT_DESC -> skins.sortedByDescending { it.floatValue ?: Double.MIN_VALUE }
        SortOption.RARITY_ASC -> skins.sortedBy { it.rarity?.ordinal ?: Int.MAX_VALUE }
        SortOption.RARITY_DESC -> skins.sortedByDescending { it.rarity?.ordinal ?: Int.MIN_VALUE }
    }
}

private fun formatPriceRub(price: Double?): String {
    if (price == null) return "—"
    val formatter = java.text.DecimalFormat("#,##0.00")
    formatter.decimalFormatSymbols = java.text.DecimalFormatSymbols(java.util.Locale("ru"))
    return "${formatter.format(price)} руб."
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SkinShowcaseTheme {
        HomeScreen(onSkinClick = { _, _ -> })
    }
}
