package ru.kotlix.skinshowcase.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.core.domain.SkinFilter
import ru.kotlix.skinshowcase.core.domain.SkinRarity
import ru.kotlix.skinshowcase.core.domain.SkinSpecial
import ru.kotlix.skinshowcase.core.domain.SkinWear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFilterSheet(
    currentFilter: SkinFilter,
    onDismiss: () -> Unit,
    onApply: (SkinFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var priceMinText by remember(currentFilter) {
        mutableStateOf(currentFilter.priceMin?.toString() ?: "")
    }
    var priceMaxText by remember(currentFilter) {
        mutableStateOf(currentFilter.priceMax?.toString() ?: "")
    }
    var floatMinText by remember(currentFilter) {
        mutableStateOf(currentFilter.floatMin?.toString() ?: "")
    }
    var floatMaxText by remember(currentFilter) {
        mutableStateOf(currentFilter.floatMax?.toString() ?: "")
    }
    var nameContains by remember(currentFilter) {
        mutableStateOf(currentFilter.nameContains)
    }
    var nameExcludesText by remember(currentFilter) {
        mutableStateOf(currentFilter.nameExcludes.joinToString(", "))
    }
    var specials by remember(currentFilter) {
        mutableStateOf(currentFilter.specials.toSet())
    }
    var patternText by remember(currentFilter) {
        mutableStateOf(currentFilter.patternIndices.joinToString(", "))
    }
    var selectedStickers by remember(currentFilter) {
        mutableStateOf(currentFilter.requiredStickerNames.toList())
    }
    var requireKeychain by remember(currentFilter) {
        mutableStateOf(currentFilter.requireKeychain)
    }
    var selectedKeychains by remember(currentFilter) {
        mutableStateOf(currentFilter.keychainNames.toList())
    }
    var rarities by remember(currentFilter) {
        mutableStateOf(currentFilter.rarities.toSet())
    }
    var selectedCollections by remember(currentFilter) {
        mutableStateOf(currentFilter.collections.toList())
    }
    var wears by remember(currentFilter) {
        mutableStateOf(currentFilter.wears.toSet())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            FilterSectionLabel(stringResource(R.string.filter_price_range))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = priceMinText,
                    onValueChange = { priceMinText = it },
                    label = { Text(stringResource(R.string.filter_price_min)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = priceMaxText,
                    onValueChange = { priceMaxText = it },
                    label = { Text(stringResource(R.string.filter_price_max)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_float_range))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = floatMinText,
                    onValueChange = { floatMinText = it },
                    label = { Text(stringResource(R.string.filter_float_min)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = floatMaxText,
                    onValueChange = { floatMaxText = it },
                    label = { Text(stringResource(R.string.filter_float_max)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_name_contains))
            OutlinedTextField(
                value = nameContains,
                onValueChange = { nameContains = it },
                placeholder = { Text(stringResource(R.string.filter_name_contains_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_name_excludes))
            OutlinedTextField(
                value = nameExcludesText,
                onValueChange = { nameExcludesText = it },
                placeholder = { Text(stringResource(R.string.filter_name_excludes_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_special))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkinSpecial.entries.forEach { spec ->
                    FilterChip(
                        selected = spec in specials,
                        onClick = {
                            specials = if (spec in specials) specials - spec
                            else specials + spec
                        },
                        label = { Text(spec.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_pattern))
            OutlinedTextField(
                value = patternText,
                onValueChange = { patternText = it },
                placeholder = { Text(stringResource(R.string.filter_pattern_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_stickers))
            StickerSelector(
                selected = selectedStickers,
                onAdd = { name -> if (name !in selectedStickers) selectedStickers = selectedStickers + name },
                onRemove = { name -> selectedStickers = selectedStickers - name }
            )

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_keychain))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf<Pair<Boolean?, String>>(
                    null to stringResource(R.string.filter_keychain_any),
                    true to stringResource(R.string.filter_keychain_yes),
                    false to stringResource(R.string.filter_keychain_no)
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = requireKeychain == value,
                        onClick = { requireKeychain = value },
                        label = { Text(label) }
                    )
                }
            }
            if (requireKeychain == true) {
                Spacer(modifier = Modifier.height(8.dp))
                KeychainSelector(
                    selected = selectedKeychains,
                    onAdd = { name -> if (name !in selectedKeychains) selectedKeychains = selectedKeychains + name },
                    onRemove = { name -> selectedKeychains = selectedKeychains - name }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_rarity))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkinRarity.entries.forEach { r ->
                    val chipColor = rarityColor(r)
                    FilterChip(
                        selected = r in rarities,
                        onClick = {
                            rarities = if (r in rarities) rarities - r else rarities + r
                        },
                        label = { Text(r.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = chipColor.paleVariant(),
                            labelColor = chipColor.darkLabelOnPale(),
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_collection))
            CollectionSelector(
                selected = selectedCollections,
                onAdd = { name -> if (name !in selectedCollections) selectedCollections = selectedCollections + name },
                onRemove = { name -> selectedCollections = selectedCollections - name }
            )

            Spacer(modifier = Modifier.height(12.dp))
            FilterSectionLabel(stringResource(R.string.filter_quality))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkinWear.entries.forEach { w ->
                    val chipColor = wearColor(w)
                    FilterChip(
                        selected = w in wears,
                        onClick = {
                            wears = if (w in wears) wears - w else wears + w
                        },
                        label = { Text(w.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = chipColor.paleVariant(),
                            labelColor = chipColor.darkLabelOnPale(),
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            val draftFilter = buildFilter(
                priceMinText = priceMinText,
                priceMaxText = priceMaxText,
                floatMinText = floatMinText,
                floatMaxText = floatMaxText,
                nameContains = nameContains,
                nameExcludesText = nameExcludesText,
                specials = specials,
                patternText = patternText,
                selectedStickers = selectedStickers,
                requireKeychain = requireKeychain,
                selectedKeychains = selectedKeychains,
                rarities = rarities,
                selectedCollections = selectedCollections,
                wears = wears
            )
            val applyEnabled = draftFilter != currentFilter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        priceMinText = ""
                        priceMaxText = ""
                        floatMinText = ""
                        floatMaxText = ""
                        nameContains = ""
                        nameExcludesText = ""
                        specials = emptySet()
                        patternText = ""
                        selectedStickers = emptyList()
                        requireKeychain = null
                        selectedKeychains = emptyList()
                        rarities = emptySet()
                        selectedCollections = emptyList()
                        wears = emptySet()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.filter_reset))
                }
                Button(
                    onClick = {
                        onApply(draftFilter)
                    },
                    enabled = applyEnabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Text(stringResource(R.string.filter_apply))
                }
            }
        }
    }
}

@Composable
private fun StickerSelector(
    selected: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        selected.forEach { name ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemove(name) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        StickerDropdown(
            hint = stringResource(R.string.filter_stickers_hint),
            onSelect = onAdd
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickerDropdown(
    hint: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val suggestions = remember(searchText) {
        filterSuggestions(searchText, STICKER_SUGGESTIONS)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it; expanded = it.isNotEmpty() },
            label = { Text(stringResource(R.string.filter_sticker_add)) },
            placeholder = { Text(hint) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            suggestions.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(name)
                        searchText = ""
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun KeychainSelector(
    selected: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        selected.forEach { name ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemove(name) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        KeychainDropdown(
            hint = stringResource(R.string.filter_keychain_hint),
            onSelect = onAdd
        )
    }
}

@Composable
private fun CollectionSelector(
    selected: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        selected.forEach { name ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemove(name) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        CollectionDropdown(
            hint = stringResource(R.string.filter_collection_hint),
            onSelect = onAdd
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDropdown(
    hint: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val suggestions = remember(searchText) {
        filterSuggestions(searchText, COLLECTION_SUGGESTIONS)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it; expanded = it.isNotEmpty() },
            label = { Text(stringResource(R.string.filter_collection_add)) },
            placeholder = { Text(hint) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            suggestions.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(name)
                        searchText = ""
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeychainDropdown(
    hint: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val suggestions = remember(searchText) {
        filterSuggestions(searchText, KEYCHAIN_SUGGESTIONS)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it; expanded = it.isNotEmpty() },
            label = { Text(stringResource(R.string.filter_keychain_add)) },
            placeholder = { Text(hint) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            suggestions.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(name)
                        searchText = ""
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

private fun buildFilter(
    priceMinText: String,
    priceMaxText: String,
    floatMinText: String,
    floatMaxText: String,
    nameContains: String,
    nameExcludesText: String,
    specials: Set<ru.kotlix.skinshowcase.core.domain.SkinSpecial>,
    patternText: String,
    selectedStickers: List<String>,
    requireKeychain: Boolean?,
    selectedKeychains: List<String>,
    rarities: Set<SkinRarity>,
    selectedCollections: List<String>,
    wears: Set<SkinWear>
): SkinFilter {
    val priceMin = priceMinText.trim().toDoubleOrNull()
    val priceMax = priceMaxText.trim().toDoubleOrNull()
    val floatMin = floatMinText.trim().toDoubleOrNull()
    val floatMax = floatMaxText.trim().toDoubleOrNull()
    val patternIndices = patternText.split(",").mapNotNull { it.trim().toIntOrNull() }
    val nameExcludes = nameExcludesText.split(",").map { it.trim() }.filter { it.isNotBlank() }

    return SkinFilter(
        priceMin = priceMin,
        priceMax = priceMax,
        floatMin = floatMin,
        floatMax = floatMax,
        nameContains = nameContains.trim(),
        nameExcludes = nameExcludes,
        specials = specials,
        patternIndices = patternIndices,
        requiredStickerNames = selectedStickers,
        requireKeychain = requireKeychain,
        keychainNames = if (requireKeychain == true) selectedKeychains else emptyList(),
        rarities = rarities,
        collections = selectedCollections,
        wears = wears
    )
}
