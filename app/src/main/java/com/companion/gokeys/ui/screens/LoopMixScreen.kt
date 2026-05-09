@file:OptIn(ExperimentalLayoutApi::class)

package com.companion.gokeys.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.companion.gokeys.R
import com.companion.gokeys.data.LoopMix
import com.companion.gokeys.ui.components.PrimaryButton
import com.companion.gokeys.ui.components.SectionCard
import com.companion.gokeys.ui.theme.LocalSliderThumb
import com.companion.gokeys.ui.theme.Muted
import com.companion.gokeys.viewmodel.CompanionViewModel

@Composable
fun LoopMixScreen(vm: CompanionViewModel) {
    val state by vm.state.collectAsState()
    val lm = state.loopmix
    var stylesExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionCard(
            title = stringResource(R.string.section_loopmix),
            badge = stringResource(R.string.badge_keys),
        ) {
            Text(stringResource(R.string.loopmix_intro))
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.loopmix_model_note),
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val accentColor = LocalSliderThumb.current
                if (!lm.running) PrimaryButton(text = stringResource(R.string.btn_start), onClick = { vm.loopMixStart() })
                else PrimaryButton(text = stringResource(R.string.btn_stop), onClick = { vm.loopMixStop() }, containerColor = accentColor)
            }
        }

        // Style card with expandable chip grid
        SectionCard {
            val accentColor = LocalSliderThumb.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.loopmix_style),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { stylesExpanded = !stylesExpanded },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        if (stylesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (stylesExpanded) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LoopMix.STYLES.forEachIndexed { i, style ->
                        val sel = i == lm.styleIdx
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(if (sel) accentColor else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { vm.loopMixSetStyle(i) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(style, color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LoopMix.STYLES.size) { i ->
                        val sel = i == lm.styleIdx
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(if (sel) accentColor else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { vm.loopMixSetStyle(i) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(LoopMix.STYLES[i], color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Key card — 2 rows of 6
        SectionCard(title = stringResource(R.string.loopmix_key)) {
            val accentColor = LocalSliderThumb.current
            val keys = LoopMix.KEYS
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0..1) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (col in 0..5) {
                            val i = row * 6 + col
                            val sel = i == lm.keyIdx
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) accentColor else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { vm.loopMixSetKey(i) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    keys[i],
                                    color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Variation card — buttons 1-7 (tapping selected deselects back to 0)
        SectionCard(title = stringResource(R.string.loopmix_variation)) {
            val accentColor = LocalSliderThumb.current
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (v in 1..7) {
                    val sel = v == lm.variation
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (sel) accentColor else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { vm.loopMixSetVariation(if (sel) 0 else v) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$v",
                            color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
