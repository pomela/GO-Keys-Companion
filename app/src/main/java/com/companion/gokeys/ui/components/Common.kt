@file:OptIn(ExperimentalMaterial3Api::class)

package com.companion.gokeys.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.companion.gokeys.ui.theme.Border
import com.companion.gokeys.ui.theme.MutedSurface
import com.companion.gokeys.ui.theme.Primary
import com.companion.gokeys.ui.theme.SliderThumb
import com.companion.gokeys.ui.theme.Success
import com.companion.gokeys.ui.theme.SurfaceVariant
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun SectionCard(title: String? = null, badge: String? = null, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (badge != null) ModelBadge(badge)
                }
                Spacer(Modifier.height(8.dp))
            }
            content()
        }
    }
}

/**
 * Section card whose body can be collapsed/expanded by tapping the header.
 * Use [initiallyExpanded] = true for sections that should default to open
 * (e.g. the main channel).  An optional [badge] (KEYS / PIANO) is rendered
 * to the right of the title.
 */
@Composable
fun ExpandableSectionCard(
    title: String,
    badge: String? = null,
    stateKey: String? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by if (stateKey != null) {
        rememberSaveable(stateKey) { mutableStateOf(initiallyExpanded) }
    } else {
        remember { mutableStateOf(initiallyExpanded) }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (badge != null) {
                    ModelBadge(badge)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Small coloured pill indicating that a feature is documented to apply only to
 * a specific Roland GO model (KEYS or PIANO).  Shared features are unbadged.
 */
@Composable
fun ModelBadge(label: String) {
    val isKeys = label.contains("KEYS", ignoreCase = true)
    val bg = if (isKeys) Primary.copy(alpha = 0.18f) else Color(0xFFB07A2A).copy(alpha = 0.22f)
    val fg = if (isKeys) Primary else Color(0xFFE0A04A)
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            label,
            color = fg,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) Primary else SurfaceVariant)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .background(MutedSurface)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun StatusDot(connected: Boolean) {
    Box(
        Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (connected) Success else Color(0xFF6B6F7B)),
    )
}

/**
 * Single-row slider: label overlaid on the left of the track, value centred,
 * reset button to the right.  Uses a bar-shaped thumb in [SliderThumb] colour.
 * Text composables don't consume pointer events, so they pass touches through
 * to the underlying Slider.
 */
@Composable
fun LabeledSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    range: IntRange = 0..127,
    onReset: (() -> Unit)? = null,
) {
    // rememberUpdatedState ensures the SliderState's onValueChangeFinished lambda
    // always calls the latest callback even though SliderState is created once.
    val finishedRef = rememberUpdatedState(onValueChangeFinished)
    val changeRef = rememberUpdatedState(onValueChange)

    val sliderState = remember(range.first, range.last) {
        SliderState(
            value = value.toFloat(),
            valueRange = range.first.toFloat()..range.last.toFloat(),
            onValueChangeFinished = { finishedRef.value?.invoke() },
        )
    }
    LaunchedEffect(sliderState) {
        snapshotFlow { sliderState.value.toInt() }
            .distinctUntilChanged()
            .collect { changeRef.value(it) }
    }
    // Sync external value changes (e.g. after Reset CC) into the slider state.
    LaunchedEffect(value) {
        if (sliderState.value.toInt() != value) sliderState.value = value.toFloat()
    }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            Slider(
                state = sliderState,
                modifier = Modifier.fillMaxWidth(),
                thumb = {
                    // Thick vertical-bar thumb in a contrasting colour
                    Box(
                        Modifier
                            .width(5.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SliderThumb),
                    )
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = Primary.copy(alpha = 0.65f),
                    inactiveTrackColor = SurfaceVariant,
                    thumbColor = SliderThumb,
                ),
            )
            // Label — left, passes through touch events to the Slider below
            Text(
                text = label,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp, end = 56.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Value — centred
            Text(
                text = value.toString(),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        if (onReset != null) {
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
                    .clickable { onReset() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * Custom vertical scrollbar for a LazyColumn — Compose has none built in.
 * Renders a thumb proportional to the visible window and lets the user
 * drag it to scroll the list.
 */
@Composable
fun LazyColumnScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val visible by remember {
        derivedStateOf {
            val info = state.layoutInfo
            info.totalItemsCount > info.visibleItemsInfo.size && info.visibleItemsInfo.isNotEmpty()
        }
    }
    BoxWithConstraints(
        modifier
            .fillMaxHeight()
            .width(10.dp)
            .padding(vertical = 2.dp),
    ) {
        if (!visible) return@BoxWithConstraints
        val total = state.layoutInfo.totalItemsCount.coerceAtLeast(1).toFloat()
        val visibleCount = state.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1).toFloat()
        val first = state.firstVisibleItemIndex.toFloat()
        val proportion = (visibleCount / total).coerceIn(0.05f, 1f)
        val offsetFraction = (first / total).coerceIn(0f, 1f - proportion)
        val trackHeight = maxHeight
        Box(
            Modifier
                .fillMaxHeight()
                .width(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceVariant)
                .pointerInput(state) {
                    detectDragGestures { _, drag ->
                        scope.launch {
                            // Scrollbar drag: thumb height = trackHeight * proportion
                            // Each pixel of drag scrolls (1/proportion) pixels of content.
                            state.scrollBy(drag.y / proportion)
                        }
                    }
                },
        ) {
            Column(Modifier.fillMaxHeight().fillMaxWidth()) {
                if (offsetFraction > 0f) {
                    Spacer(Modifier.fillMaxWidth().height(trackHeight * offsetFraction))
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(trackHeight * proportion)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Primary),
                )
            }
        }
    }
}
