# AK Tool

A Kotlin Multiplatform companion app for managing a mobile-game account against an
**admin HTTP API that you host yourself**. It ships a desktop (JVM) application and an
Android application that share one code base: UI, domain logic, networking and storage.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Modules](#modules)
- [Requirements](#requirements)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Downloaded data tables](#downloaded-data-tables)
- [Architecture](#architecture)
- [Project layout](#project-layout)
- [Tests](#tests)
- [Related documents](#related-documents)
- [Notice](#notice)
- [License](#license)

---

## Features

**Account status (`Home`)**
View and edit account-level values — nickname, level, and the various in-game counters —
with pull-to-refresh against `admin/status/sync` / `admin/status/save`.

**Character roster (`Character`)**
A grid of the account's characters, with a profession filter and a name search. Cards show
artwork, rarity, elite/level badges and skill/equipment state.

**Character detail (`CharacterDetail`)**
Per-character editing: level, potential, favour points, skills and equipment. Saving writes
back through `admin/character/save`. The card animates from its grid cell into the detail
screen using Compose shared-element transitions.

**Bulk actions and account management (`Extra`)**
Unlock-all helpers (characters / stages / flags), item grants, account registration and
account reset.

**Settings (`Setting`)**
Server URI, credentials, theme (dark mode, dynamic colour, custom seed colour) and a custom
background image. Also the entry point for refreshing the downloaded data tables.

**Desktop and large-screen behaviour**
The desktop window is sized from the screen work area (landscape, centred). From 600dp of
window width the bottom tab bar becomes a left-hand navigation rail, and pages that can use
the extra room lay their items out two per row. The large-screen decision is made once, in
`AppNavHost`, and published to pages through `LocalLargeScreen`.

---

## Tech stack

| Area | Choice | Version |
|---|---|---|
| Language | Kotlin (Multiplatform) | 2.4.20 |
| UI | Compose Multiplatform | 1.12.1 |
| Design system | Miuix (KMP) | 0.9.4 |
| Material 3 components | `org.jetbrains.compose.material3` | 1.9.0 |
| Android Gradle Plugin | AGP | 9.4.1 |
| Build | Gradle wrapper | 9.6.0 |
| HTTP client | Ktor client | 3.6.0 |
| JSON | kotlinx.serialization | 1.11.0 |
| Coroutines | kotlinx.coroutines | 1.11.0 |
| DI | Koin | 4.2.2 |
| Navigation | Navigation3 (back stack + scenes) | 1.2.0-beta01 |
| Images | Coil 3 | 3.6.3 |
| Logging | Kermit | 2.2.0 |
| Testing | kotlin-test, JUnit 4 | 4.13.2 |

Android targets `compileSdk`/`targetSdk` **37**, `minSdk` **28**, `versionCode` **18**,
`versionName` **1.5.6**.

---

## Modules

| Module | Type | Targets | Notes |
|---|---|---|---|
| `:shared` | Kotlin Multiplatform library | `android`, `jvm("desktop")` | All UI, domain, data and design-system code. Namespace `com.rainccup.aktool.shared`. |
| `:androidApp` | Android application | single `main` source set | Thin host: entry activity, manifest, app theme. |
| `:desktopApp` | JVM application | single `main` source set | Compose Desktop host; `mainClass = com.rainccup.aktool.MainKt`. |

The Compose resource class for `:shared` is generated into `com.rainccup.aktool.resources`.

---

## Requirements

- **JDK 21** for `:desktopApp` (`jvmToolchain(21)`). `:shared` and `:androidApp` compile with
  `jvmTarget` 17; the Foojay toolchain resolver is enabled, so Gradle can provision the JDK.
- **Android SDK** with API 37 platform, referenced from `local.properties` (`sdk.dir=…`).
  `local.properties` is git-ignored and must be created locally.
- Network access to your own server. The Android app permits cleartext HTTP
  (`android:usesCleartextTraffic="true"` — see [Configuration](#configuration)).

---

## Getting started

```bash
# Desktop application
./gradlew :desktopApp:run

# Android application
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug      # to a connected device/emulator

# Tests
./gradlew :shared:allTests              # every target's tests
./gradlew :shared:desktopTest           # desktop target only (includes the architecture test)
```

On Windows use `gradlew.bat`. `:desktopApp` is a standard Compose Desktop application block,
so the Compose Gradle plugin's packaging tasks apply as well (for example
`packageDistributionForCurrentOS`); `nativeDistributions` is configured with
`packageName = "AK Tool"` and `packageVersion = "1.5.6"`.

Release builds of `:androidApp` enable `isMinifyEnabled` and `isShrinkResources`. No
`signingConfigs` are declared in this repository, so a release APK/AAB needs signing details
supplied by you.

---

## Configuration

The app persists a single JSON document, created with defaults on first launch:

| Platform | Path |
|---|---|
| Desktop | `~/.aktool/config/config.json` |
| Android | `<app filesDir>/config/config.json` |

```jsonc
{
  "serverUri": "",        // base URL of your admin API; blank falls back to http://127.0.0.1
  "uid": "",              // sent as the `uid` header
  "adminKey": "",         // sent as the `adminKey` header
  "darkMode": false,
  "dynamicColor": true,
  "customBg": false,
  "bgPath": "",           // absolute path of the chosen background image
  "primaryColor": "0x0"   // ULong; 0 means "no custom seed colour"
}
```

Everything except the API base URL can be edited on the **Settings** screen. Saving
credentials goes through `ConfigStore`, which rewrites `config.json` and recreates the cached
HTTP client so the new base URL takes effect immediately.

**Security notes**

- `uid` and `adminKey` are stored in plain text in `config.json` and sent as plain HTTP
  headers. Treat the file as a secret.
- The Android build allows cleartext traffic because the default deployment target is a
  machine on your own network. Put the API behind TLS (and remove the cleartext allowance)
  before exposing it beyond a trusted network.

---

## Downloaded data tables

Static game data is not bundled. The app fetches three JSON files from your server and caches
them on disk:

```
<serverUri>/assetbundle/excel/character_table.json
<serverUri>/assetbundle/excel/favor_table.json
<serverUri>/assetbundle/excel/uniequip_table.json
```

They are written to `<dataDir>/excel/` (`~/.aktool/data/excel/` on desktop,
`<filesDir>/data/excel/` on Android) and refreshed from the Settings screen. The client reads
only a few fields from each table (elite phases and their max level; favour frames and their
percent values; equipment type icons) and treats everything else as opaque. Fetching is plain
`GET` with no `uid`/`adminKey` headers — see [`API.md`](API.md).

---

## Architecture

Code is layered **by package**, not by Gradle module, so the compiler cannot enforce the
dependency direction. Instead a test does: `ArchitectureRuleTest` scans every `*.kt` file under
`shared/src`, derives a layer name from each `package` header and each
`import com.rainccup.aktool.*`, and fails if an edge is not in its allow-list.

Target layering:

```
app            -> everything (the shell wires the layers together)
feature.*      -> core.domain, core.designsystem, core.model, core.navigation, core.common
core.domain    -> core.data, core.model, core.common
core.data      -> core.network, core.model, core.common, core.platform
core.designsystem -> core.model, core.common
core.navigation   -> core.model
core.network / core.message / core.platform -> core.model, core.common
```

Only the app shell may import `feature.*`, no feature may import another feature, and features
reach data exclusively through use cases.

Three deviations are **intentional and documented in the test itself**:

1. `feature.* -> core.platform` — UI uses platform ports (`platformUiScale`, `urlEncode`,
   `Messenger`, `ClipboardPort`) which are `expect`/`actual` declarations.
2. `feature.characterdetail -> feature.character` — the detail screen reuses the roster's card
   and painters and holds `CharacterViewModel` to refresh the list after saving.
3. `core.common <-> core.model` — `core.common` holds `LocalAppConfig`, which carries the
   `AppConfig` model; the reverse edge comes from tests only.

The allow-list is a **ratchet**: it started as the pre-migration status quo and is expected to
shrink toward the target model. When the test fails it prints each offending edge, which you
either fix or register deliberately.

---

## Project layout

```
shared/src/commonMain/kotlin/com/rainccup/aktool/
├── app/                     application shell, navigation host, DI modules
│   ├── AppNavHost.kt        Scaffold, tabs/rail, transition specs
│   └── ...
├── core/
│   ├── common/              small shared helpers (validation, JSON, map/list utils)
│   ├── data/                repositories, data sources, file I/O, DI wiring
│   ├── designsystem/        theme, shared components (action rows, sliders, dialogs)
│   ├── domain/              use cases (the only entry point features may use for data)
│   ├── message/             message bus feeding the snackbar host
│   ├── model/               wire models (Character, Status, requests, AppConfig)
│   ├── navigation/          routes, navigation motion (direction math for transitions)
│   ├── network/             HTTP client factory, ApiClient, network config
│   └── platform/            expect/actual ports (paths, scale, file picker, clipboard)
└── feature/                 one package per screen: home, character, characterdetail,
                             extra, setting, splash — each with `ui/` and `viewmodel/`
```

Platform source sets provide the `actual` implementations:
`shared/src/androidMain` (OkHttp engine, Android paths, SAF pickers) and
`shared/src/desktopMain` (CIO engine, `~/.aktool` paths, AWT file pickers and work-area sizing).

---

## Tests

| Source set | What it covers |
|---|---|
| `shared/src/commonTest` | Model serialization, use cases, repositories, navigation motion, message bus, DI resolution, API client against Ktor's `MockEngine`. |
| `shared/src/desktopTest` | The package-dependency ratchet (`ArchitectureRuleTest`). |

```bash
./gradlew :shared:allTests
```

---

## Related documents

- [`API.md`](API.md) — the HTTP interface: transport setup, authentication, the
  response envelope, every endpoint, and the request/response models.

---

## Notice

This project is not affiliated with, endorsed by, or associated with any game publisher. It
contains no game assets; The admin API it talks to is one you
host and are responsible for, and you are responsible for how you use it and any account data
you access.

---

## License

Released under the **MIT License** — see [`LICENSE`](LICENSE).

Copyright (c) 2026 Cryin.
