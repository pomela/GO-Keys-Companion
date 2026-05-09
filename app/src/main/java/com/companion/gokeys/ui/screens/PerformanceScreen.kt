@file:OptIn(ExperimentalMaterial3Api::class)

package com.companion.gokeys.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.companion.gokeys.R
import com.companion.gokeys.data.AppState
import com.companion.gokeys.data.PartConfig
import com.companion.gokeys.data.Patches
import com.companion.gokeys.ui.components.ExpandableSectionCard
import com.companion.gokeys.ui.components.GhostButton
import com.companion.gokeys.ui.components.LabeledSlider
import com.companion.gokeys.ui.components.PatchLibrary
import com.companion.gokeys.ui.components.PrimaryButton
import com.companion.gokeys.ui.components.SectionCard
import com.companion.gokeys.ui.theme.Border
import com.companion.gokeys.ui.theme.LocalSliderThumb
import com.companion.gokeys.ui.theme.Muted
import com.companion.gokeys.viewmodel.CompanionViewModel

@Composable
fun PerformanceScreen(vm: CompanionViewModel) {
    val state by vm.state.collectAsState()
    val perf = state.performance
    var selectedPart by rememberSaveable { mutableStateOf(0) }
    var showPatchSheet by remember { mutableStateOf(false) }
    var showDemoSheet by remember { mutableStateOf(false) }
    val patchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val demoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenH = LocalConfiguration.current.screenHeightDp

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        // 1. Collapsible intro card
        ExpandableSectionCard(
            title = stringResource(R.string.section_performance),
            stateKey = "perf-intro",
            initiallyExpanded = false,
        ) {
            Text(stringResource(R.string.performance_intro), color = Muted)
        }

        // 2. Master controls — always visible above the part selector
        MasterStrip(state = state, vm = vm, onShowDemo = { showDemoSheet = true })

        // Part selector row
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (i in perf.parts.indices) {
                val shortTitle = when (i) {
                    0 -> "Main"; 1 -> "Layer"; 2 -> "Split"; else -> "Part ${i + 1}"
                }
                val part = perf.parts[i]
                val patch = Patches.find(part.patchMsb, part.patchLsb, part.patchPc)
                PartSelectorCard(
                    modifier = Modifier.weight(1f),
                    title = shortTitle,
                    patchName = patch?.name ?: "—",
                    isSelected = selectedPart == i,
                    isActive = perf.partsEnabled[i],
                    onClick = { selectedPart = i; showPatchSheet = false },
                )
            }
        }

        // 3. Controls for the selected part (sound shaping opens as sheet)
        SectionCard {
            PartControlPanel(
                vm = vm,
                partIndex = selectedPart,
                onPickSound = { showPatchSheet = true },
            )
        }

        Spacer(Modifier.height(20.dp))
    }

    if (showPatchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPatchSheet = false },
            sheetState = patchSheetState,
        ) {
            PatchLibrary(
                vm = vm,
                partIndex = selectedPart,
                modifier = Modifier.padding(horizontal = 12.dp),
                minListHeight = (screenH * 0.50f).dp,
                onPicked = { showPatchSheet = false },
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDemoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDemoSheet = false },
            sheetState = demoSheetState,
        ) {
            DemoSheetContent(vm = vm, onDismiss = { showDemoSheet = false })
        }
    }
}

// Always-visible master volume + transport controls
@Composable
private fun MasterStrip(state: AppState, vm: CompanionViewModel, onShowDemo: () -> Unit) {
    SectionCard {
        LabeledSlider(
            stringResource(R.string.slider_master_vol), state.master.masterVolume,
            onValueChange = { v -> vm.updateMaster { it.copy(masterVolume = v) } },
            onValueChangeFinished = { vm.pushMasterVolume() },
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TempoControl(
                nudge = state.master.tempoNudge,
                onDown = { vm.tempoDown() },
                onUp = { vm.tempoUp() },
            )
            Box(Modifier.weight(1f))
            PrimaryButton(stringResource(R.string.btn_panic), onClick = { vm.panic() })
            PrimaryButton("Demo", onClick = onShowDemo)
        }
    }
}

@Composable
private fun TempoControl(nudge: Int, onDown: () -> Unit, onUp: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        PrimaryButton("−", onClick = onDown)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Text("Tempo", style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(
                if (nudge > 0) "+$nudge" else "$nudge",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        PrimaryButton("+", onClick = onUp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DemoSheetContent(vm: CompanionViewModel, onDismiss: () -> Unit) {
    val playingDemo by vm.playingDemo.collectAsState()
    val accentColor = LocalSliderThumb.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(stringResource(R.string.section_demo), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.demo_intro), color = Muted)
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (d in 0..4) {
                PrimaryButton(
                    text = stringResource(R.string.demo_song, d + 1),
                    onClick = { vm.playDemoSong(d); onDismiss() },
                    containerColor = if (playingDemo == d) accentColor else null,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        GhostButton(stringResource(R.string.btn_demo_off), onClick = { vm.demoOff(); onDismiss() })
    }
}

@Composable
private fun PartSelectorCard(
    title: String,
    patchName: String,
    isSelected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainColor = MaterialTheme.colorScheme.primary
    val bg = when {
        isSelected -> mainColor.copy(alpha = 0.25f)
        isActive -> mainColor.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val nameColor = if (isActive || isSelected) MaterialTheme.colorScheme.onSurface else Muted
    val patchColor = when {
        isSelected -> mainColor
        isActive -> mainColor.copy(alpha = 0.75f)
        else -> Muted.copy(alpha = 0.5f)
    }

    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .then(if (isSelected) Modifier.border(1.dp, mainColor, RoundedCornerShape(12.dp)) else Modifier)
            .clickable { onClick() }
            .padding(8.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = nameColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                patchName,
                style = MaterialTheme.typography.labelSmall,
                color = patchColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PartControlPanel(
    vm: CompanionViewModel,
    partIndex: Int,
    onPickSound: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val perf = state.performance
    val enabled = perf.partsEnabled[partIndex]
    val part = perf.parts[partIndex]
    val patch = Patches.find(part.patchMsb, part.patchLsb, part.patchPc)
    val zone = perf.zones[partIndex]
    // Reset sheet state when switching parts
    var showShapingSheet by remember(partIndex) { mutableStateOf(false) }
    val shapingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.part_active), Modifier.weight(1f))
        Switch(checked = enabled, onCheckedChange = { vm.setPartEnabled(partIndex, it) })
    }
    Spacer(Modifier.height(6.dp))

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.part_channel, part.channel), color = Muted, modifier = Modifier.weight(1f))
        GhostButton(text = patch?.name ?: stringResource(R.string.btn_pick_sound), onClick = onPickSound)
    }

    Spacer(Modifier.height(12.dp))

    LabeledSlider(
        stringResource(R.string.slider_volume), part.volume,
        onValueChange = { v -> vm.updatePart(partIndex) { it.copy(volume = v) } },
        onValueChangeFinished = { vm.pushVolume(partIndex) },
        onReset = { vm.resetVolume(partIndex) }, defaultValue = 110,
    )
    LabeledSlider(
        stringResource(R.string.slider_pan), part.pan, range = 0..127,
        onValueChange = { v -> vm.updatePart(partIndex) { it.copy(pan = v) } },
        onValueChangeFinished = { vm.pushPan(partIndex) },
        onReset = { vm.resetPan(partIndex) }, defaultValue = 64,
    )
    LabeledSlider(
        stringResource(R.string.slider_reverb), part.reverb,
        onValueChange = { v -> vm.updatePart(partIndex) { it.copy(reverb = v) } },
        onValueChangeFinished = { vm.pushReverb(partIndex) },
        onReset = { vm.resetReverb(partIndex) }, defaultValue = 40,
    )
    LabeledSlider(
        stringResource(R.string.slider_chorus), part.chorus,
        onValueChange = { v -> vm.updatePart(partIndex) { it.copy(chorus = v) } },
        onValueChangeFinished = { vm.pushChorus(partIndex) },
        onReset = { vm.resetChorus(partIndex) }, defaultValue = 0,
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.zone_split), Modifier.weight(1f))
        Switch(checked = zone.enabled, onCheckedChange = { vm.setZoneEnabled(partIndex, it) })
    }
    if (zone.enabled) {
        LabeledSlider(
            stringResource(R.string.zone_low, midiNote(zone.keyLow)), zone.keyLow, range = 0..127,
            onValueChange = { v -> vm.updateZone(partIndex) { it.copy(keyLow = v) } },
            onValueChangeFinished = { vm.pushZone(partIndex) },
            defaultValue = 0,
        )
        LabeledSlider(
            stringResource(R.string.zone_high, midiNote(zone.keyHigh)), zone.keyHigh, range = 0..127,
            onValueChange = { v -> vm.updateZone(partIndex) { it.copy(keyHigh = v) } },
            onValueChangeFinished = { vm.pushZone(partIndex) },
            defaultValue = 127,
        )
    }

    Spacer(Modifier.height(12.dp))

    PrimaryButton(
        text = stringResource(R.string.section_sound_shaping),
        onClick = { showShapingSheet = true },
    )

    if (showShapingSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShapingSheet = false },
            sheetState = shapingSheetState,
        ) {
            SoundShapingSheet(vm = vm, partIndex = partIndex, part = part)
        }
    }
}

@Composable
private fun SoundShapingSheet(vm: CompanionViewModel, partIndex: Int, part: PartConfig) {
    val accentColor = LocalSliderThumb.current
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = accentColor,
        checkedTrackColor = accentColor.copy(alpha = 0.5f),
        checkedBorderColor = accentColor.copy(alpha = 0.7f),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(stringResource(R.string.section_sound_shaping), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.sound_shaping_intro), color = Muted)
        Spacer(Modifier.height(8.dp))
        LabeledSlider(
            stringResource(R.string.slider_expression), part.expression,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(expression = v) } },
            onValueChangeFinished = { vm.pushExpression(partIndex) },
            onReset = { vm.resetExpression(partIndex) }, defaultValue = 127,
        )
        LabeledSlider(
            stringResource(R.string.slider_cutoff), part.cutoff,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(cutoff = v) } },
            onValueChangeFinished = { vm.pushCutoff(partIndex) },
            onReset = { vm.resetCutoff(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_resonance), part.resonance,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(resonance = v) } },
            onValueChangeFinished = { vm.pushResonance(partIndex) },
            onReset = { vm.resetResonance(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_attack), part.attack,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(attack = v) } },
            onValueChangeFinished = { vm.pushAttack(partIndex) },
            onReset = { vm.resetAttack(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_decay), part.decay,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(decay = v) } },
            onValueChangeFinished = { vm.pushDecay(partIndex) },
            onReset = { vm.resetDecay(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_release), part.release,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(release = v) } },
            onValueChangeFinished = { vm.pushRelease(partIndex) },
            onReset = { vm.resetRelease(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_vibrato_rate), part.vibratoRate,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(vibratoRate = v) } },
            onValueChangeFinished = { vm.pushVibratoRate(partIndex) },
            onReset = { vm.resetVibratoRate(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_vibrato_depth), part.vibratoDepth,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(vibratoDepth = v) } },
            onValueChangeFinished = { vm.pushVibratoDepth(partIndex) },
            onReset = { vm.resetVibratoDepth(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_vibrato_delay), part.vibratoDelay,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(vibratoDelay = v) } },
            onValueChangeFinished = { vm.pushVibratoDelay(partIndex) },
            onReset = { vm.resetVibratoDelay(partIndex) }, defaultValue = 64,
        )
        LabeledSlider(
            stringResource(R.string.slider_portamento_time), part.portamentoTime,
            onValueChange = { v -> vm.updatePart(partIndex) { it.copy(portamentoTime = v) } },
            onValueChangeFinished = { vm.pushPortamentoTime(partIndex) },
            onReset = { vm.resetPortamentoTime(partIndex) }, defaultValue = 0,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.toggle_portamento), Modifier.weight(1f))
            Switch(
                checked = part.portamentoOn,
                onCheckedChange = {
                    vm.updatePart(partIndex) { p -> p.copy(portamentoOn = it) }
                    vm.pushPortamentoOnOff(partIndex)
                },
                colors = switchColors,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.toggle_mono), Modifier.weight(1f))
            Switch(
                checked = part.mono,
                onCheckedChange = {
                    vm.updatePart(partIndex) { p -> p.copy(mono = it) }
                    vm.pushMonoMode(partIndex)
                },
                colors = switchColors,
            )
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton(text = stringResource(R.string.btn_reset_cc), onClick = { vm.resetPartCC(partIndex) })
    }
}

private fun midiNote(n: Int): String {
    val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val oct = n / 12 - 1
    return "${names[n % 12]}$oct"
}
