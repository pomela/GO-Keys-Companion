package com.companion.gokeys.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.companion.gokeys.R
import com.companion.gokeys.data.PreferencesConfig
import com.companion.gokeys.data.ThemeMode
import com.companion.gokeys.ui.components.GhostButton
import com.companion.gokeys.ui.components.PrimaryButton
import com.companion.gokeys.ui.components.SectionCard
import com.companion.gokeys.ui.theme.Border
import com.companion.gokeys.ui.theme.Muted
import com.companion.gokeys.ui.theme.PrimaryPresets
import com.companion.gokeys.ui.theme.ThumbPresets
import com.companion.gokeys.viewmodel.CompanionViewModel

@Composable
fun ProfilesScreen(vm: CompanionViewModel) {
    val profiles by vm.profiles.collectAsState()
    val prefs by vm.preferences.collectAsState()
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        PreferencesCard(prefs = prefs, onUpdate = { vm.updatePreferences(it) })
        SectionCard(title = stringResource(R.string.section_profiles)) {
            Text(stringResource(R.string.profiles_intro), color = Muted)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = name, onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.profile_name)) },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = desc, onValueChange = { desc = it },
                placeholder = { Text(stringResource(R.string.profile_desc)) },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            PrimaryButton(text = stringResource(R.string.btn_save), onClick = {
                if (name.isNotBlank()) { vm.saveProfile(name, desc); name = ""; desc = "" }
            })
        }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            items(profiles, key = { it.id }) { p ->
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                            if (p.description.isNotBlank())
                                Text(p.description, color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                        PrimaryButton(text = stringResource(R.string.btn_load), onClick = { vm.loadProfile(p.id) })
                        Spacer(Modifier.width(6.dp))
                        GhostButton(text = stringResource(R.string.btn_delete), onClick = { vm.deleteProfile(p.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferencesCard(prefs: PreferencesConfig, onUpdate: (PreferencesConfig) -> Unit) {
    SectionCard(title = stringResource(R.string.section_preferences)) {
        Text(stringResource(R.string.pref_theme), style = MaterialTheme.typography.labelMedium, color = Muted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                val label = when (mode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.pref_theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.pref_theme_light)
                    ThemeMode.DARK -> stringResource(R.string.pref_theme_dark)
                }
                if (prefs.themeMode == mode) {
                    PrimaryButton(label, onClick = {})
                } else {
                    GhostButton(label, onClick = { onUpdate(prefs.copy(themeMode = mode)) })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.pref_primary_color), style = MaterialTheme.typography.labelMedium, color = Muted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryPresets.forEachIndexed { index, color ->
                ColorDot(
                    color = color,
                    selected = prefs.mainPresetIndex == index,
                    onClick = { onUpdate(prefs.copy(mainPresetIndex = index, mainCustomHex = "")) },
                )
            }
            ColorDot(
                color = MaterialTheme.colorScheme.onSurface,
                selected = prefs.mainPresetIndex < 0,
                onClick = { onUpdate(prefs.copy(mainPresetIndex = -1, mainCustomHex = "")) },
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.pref_thumb_color), style = MaterialTheme.typography.labelMedium, color = Muted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThumbPresets.forEachIndexed { index, color ->
                ColorDot(
                    color = color,
                    selected = prefs.accentPresetIndex == index,
                    onClick = { onUpdate(prefs.copy(accentPresetIndex = index, accentCustomHex = "")) },
                )
            }
            ColorDot(
                color = MaterialTheme.colorScheme.onSurface,
                selected = prefs.accentPresetIndex < 0,
                onClick = { onUpdate(prefs.copy(accentPresetIndex = -1, accentCustomHex = "")) },
            )
        }
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                else Modifier.border(1.dp, Border, CircleShape)
            )
            .clickable { onClick() },
    )
}
