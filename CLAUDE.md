# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GO:Keys Companion is a native Android app providing a companion interface for Roland GO:KEYS and GO:PIANO keyboards over USB MIDI and Bluetooth LE MIDI. The app enables advanced features like layering, sound shaping, macros, automations, and saved profiles that aren't easily accessible from the keyboard itself.

**Tech Stack:**
- Kotlin + Jetpack Compose (Material 3)
- Android 11+ (minSdk 29, targetSdk 35)
- Gradle 8.7, JDK 17
- Key libraries: AndroidX Navigation, DataStore (JSON persistence), Coroutines, kotlinx-serialization

## Build & Run

```bash
# Build debug APK (Linux/macOS/Git Bash)
./gradlew assembleDebug
# On Windows PowerShell: .\gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# The Gradle wrapper JAR is not included; after cloning, run:
gradle wrapper --gradle-version 8.7
# Android Studio will also offer to download it on first open.
```

Requirements: Android Studio Hedgehog+, JDK 17, Android SDK 35.

## Architecture

The app follows a single-ViewModel pattern with MVVM-like separation:

### Layer Structure

**UI Layer** (`ui/`)
- Entry point: `App.kt` (Compose NavHost with 8 bottom-nav screens)
- Screen implementations in `screens/` (Connection, Performance, LoopMix, Profiles, Macros, Automations, Monitor, Help)
- Common components in `components/Common.kt` (SectionCard, sliders, LazyColumnScrollbar) and `components/PatchLibrary.kt` (inline patch picker with scrollbar used in PerformanceScreen)
- Theme in `theme/` (Material 3 dark, color scheme)

**ViewModel Layer** (`viewmodel/CompanionViewModel.kt`)
- Single `CompanionViewModel` drives all screens via StateFlow observables
- Coordinates between UI, data persistence (Repository), and MIDI service
- Manages macro recording state and automation engine lifecycle

**Data Layer** (`data/`)
- `AppState.kt`: Core domain models (PerformanceConfig, PartConfig, ZoneConfig, MasterConfig, LoopMixConfig)
- `Patches.kt` / `HiddenPatches.kt`: ~1380 stock patches for GO:KEYS/GO:PIANO (with hidden GM2 + bonus banks)
- `LoopMix.kt`: Styles and keys for LoopMix engine
- `Repository.kt`: DataStore-backed JSON persistence (AppState, Profiles, Macros, Automations)

**MIDI Layer** (`midi/`)
- `MidiService.kt`: USB + BLE transport, send queue, rolling 200-event monitor buffer (backed by SharedFlow)
- `BleMidiClient.kt`: Custom BLE MIDI 1.0 GATT client (bypasses Android's broken `openBluetoothDevice`)
- `RolandSysEx.kt`: DT1 SysEx builder with proper checksum; high-level helpers for Performance Parts, Zones, NRPN, effects

**Automation Layer** (`automation/AutomationEngine.kt`)
- Listens to MidiService incoming events and fires declarative trigger→action rules
- Supports: noteOn/CC/SysEx prefix/patch-change triggers; send raw bytes/play macro/panic/switch patch/toggle LoopMix actions

### Key Architectural Patterns

**State Management:**
- Each major subsystem publishes StateFlow<T> observables
- ViewModel collects and re-publishes to UI (e.g., `repo.stateFlow` → `_state`)
- UI reads from ViewModel's StateFlows with `.collectAsState()`

**Persistence:**
- DataStore prefs (keys in `Repository.Keys`):
  - `app_state`: Full AppState (Performance + Master + LoopMix config)
  - `profiles`: List<Profile> (named snapshots of AppState)
  - `macros`: List<Macro> (recorded MIDI sequences with timing)
  - `automations`: List<Automation> (trigger→action rules)
- JSON serialization via kotlinx-serialization

**MIDI Transport:**
- MidiService manages both USB (via Android MidiManager) and BLE MIDI connections
- Outgoing messages are queued (Channel capacity 64) to avoid dropping
- Incoming events flow through `service.events: SharedFlow<MidiMonitorEntry>`

**SysEx/DT1:**
- RolandSysEx.dt1(...) builds properly checksummed DT1 messages
- Addresses for Performance Parts (0x10000000 base, +0x100 stride) and Zones (0x10005000) are documented constants
- Custom address adjustments can be made in RolandSysEx without rebuilding the UI

**Macros & Automations:**
- Macros record timing-aware MIDI sequences; played back via SharedFlow
- AutomationEngine watches incoming MIDI and fires actions on pattern match
- Both support declarative definitions (JSON serializable)

## Key Implementation Details

**Layering on GO:KEYS/GO:PIANO:**
- The keyboard's factory firmware ignores MIDI Channel 2 unless the Performance section exposes that Part
- The app fixes this by using Roland DT1 commands to enable Performance Parts and assign channels (documented in RolandSysEx)

**Custom UI Components:**
- `LazyColumnScrollbar`: Draggable scrollbar for the patch list (>55% of screen height reserved)
- `SectionCard`: Collapsible card for Part sections (Layer/Split/Extra parts collapse their CC controls with them)

**Persistent UI State:**
- LibraryUiState (selectedCategory, searchQuery, scroll position) is saved to AppState and restored across launches

**Sound Shaping:**
- Every CC documented in rolandgo-hacking is exposed per part:
  - Expression (CC11), Filter Cutoff/Resonance (CC74/CC71)
  - Attack/Decay/Release (CC73/75/72)
  - Vibrato Rate/Depth/Delay (CC76/77/78)
  - Portamento Time + on/off (CC5/65)
  - Mono/Poly (CC126/127)
- Per-part inline reset buttons immediately push CC changes (no slider nudging required)

**Demo Songs:**
- GO:KEYS only (GO:PIANO has one-shot LoopMix trigger)
- Triggered via exact SysEx documented in rolandgo-hacking
- Badged in UI per model

## Dependency Graph

- MainActivity → CompanionViewModel
- CompanionViewModel → {MidiService, Repository, AutomationEngine}
- AutomationEngine → MidiService
- MidiService → {BleMidiClient, RolandSysEx}
- Repository → DataStore (via Context)
- All screens → CompanionViewModel

## Testing & Development Notes

- No automated tests currently; manual testing on Android 11+ is the standard
- Bluetooth permissions are requested at app launch (Build.VERSION.SDK_INT >= S requires BLUETOOTH_SCAN + BLUETOOTH_CONNECT; older APIs use ACCESS_FINE_LOCATION)
- The MIDI monitor (MonitorScreen) shows a live 200-event rolling buffer—useful for debugging MIDI communication
- When adding new MIDI CC controls, update RolandSysEx with high-level helpers and expose in PerformanceScreen

## Additional Resources

- **rolandgo-hacking** (waldt): https://github.com/waldt/rolandgo-hacking
- **goplus** (waldt): https://github.com/waldt/goplus
- All SysEx addresses, bank/program assignments, and NRPN definitions are derived from these public docs

