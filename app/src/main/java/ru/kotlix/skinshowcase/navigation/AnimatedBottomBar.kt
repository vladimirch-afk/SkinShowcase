package ru.kotlix.skinshowcase.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val INDICATOR_ANIMATION_DURATION_MS = 250
private val BAR_TOP_RADIUS = 24.dp
private val BOTTOM_BAR_HEIGHT = 64.dp
/** Высота плашки только под иконку — иначе при центрировании по всей панели низ заходит на подписи. */
private val INDICATOR_HEIGHT = 32.dp
private val INDICATOR_WIDTH_DP = 80.dp
private val INDICATOR_CORNER_RADIUS = 14.dp
private val ITEM_VERTICAL_PADDING = 8.dp
private val ICON_SIZE = 24.dp

@Composable
fun AnimatedBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = BottomNavItem.entries
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceIn(0, items.lastIndex)
    val animatedPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(INDICATOR_ANIMATION_DURATION_MS),
        label = "tab_indicator"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = BAR_TOP_RADIUS, topEnd = BAR_TOP_RADIUS)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(BOTTOM_BAR_HEIGHT)
        ) {
            val density = LocalDensity.current
            val itemWidthPx = with(density) { (maxWidth / items.size).toPx() }
            val indicatorWidthPx = with(density) { INDICATOR_WIDTH_DP.toPx() }
            val indicatorOffsetPx = (animatedPosition * itemWidthPx) + (itemWidthPx - indicatorWidthPx) / 2f
            val indicatorTopYPx = with(density) {
                (ITEM_VERTICAL_PADDING + (ICON_SIZE - INDICATOR_HEIGHT) / 2).toPx()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BOTTOM_BAR_HEIGHT)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                indicatorOffsetPx.roundToInt(),
                                indicatorTopYPx.roundToInt()
                            )
                        }
                        .size(
                            width = INDICATOR_WIDTH_DP,
                            height = INDICATOR_HEIGHT
                        )
                        .clip(RoundedCornerShape(INDICATOR_CORNER_RADIUS))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BOTTOM_BAR_HEIGHT)
                        .padding(vertical = ITEM_VERTICAL_PADDING),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        val contentColor = barItemContentColor(selected)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (!selected) {
                                        onTabSelected(item.route)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedBottomBarItemContent(
                                item = item,
                                contentColor = contentColor,
                                selected = selected
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun barItemContentColor(selected: Boolean): Color =
    if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun AnimatedBottomBarItemContent(
    item: BottomNavItem,
    contentColor: Color,
    selected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = stringResource(item.labelRes),
            modifier = Modifier.size(ICON_SIZE),
            tint = contentColor
        )
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}
