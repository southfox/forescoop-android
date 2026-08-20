# Ventus for Android

Android port of the Ventus/Forescoop weather app (the Swift/SwiftUI original
lives at `../forescoop`). This is a **separate project** in its own
directory/git history, started fresh rather than mixed into the Swift
package.

## Status: bare scaffold (step 1)

This commit is intentionally minimal: a Gradle project that opens, syncs,
and runs a single Compose screen with no Windguru-specific code yet. Nothing
here has been built or run — there's no Android SDK/Gradle toolchain in the
environment that generated it, so **the first thing to do is open it in
Android Studio and confirm it actually builds** before trusting any of it.

- Kotlin + Jetpack Compose + Material3
- `applicationId` / package: `org.forescoop.ventus`
- `minSdk` 26, `targetSdk`/`compileSdk` 35
- AGP 8.7.0, Kotlin 2.0.21, Gradle 8.10.2 (via version catalog:
  `gradle/libs.versions.toml`)

## One-time setup

The Gradle wrapper jar (`gradle/wrapper/gradle-wrapper.jar`) is **not**
included — it's a binary I can't safely hand-author or verify without a
network connection. To materialize it:

- **Easiest:** open this folder in Android Studio (`File > Open`). Android
  Studio bundles its own Gradle/JDK and will offer to generate/repair the
  wrapper automatically on first sync.
- **Or, from a terminal**, if you have Gradle installed locally (e.g. via
  Homebrew or SDKMAN):
  ```
  cd forescoop-android
  gradle wrapper --gradle-version 8.10.2
  ```
  This regenerates `gradlew`, `gradlew.bat`, and the wrapper jar to match
  `gradle-wrapper.properties`.

No launcher icon resources are included yet either (kept the scaffold
buildable without needing generated PNG/adaptive-icon assets) — Android
Studio's Asset Studio (`New > Image Asset`) is the easiest way to add real
ones later.

## Planned steps (roughly in order)

1. **Bare scaffold** ← you are here.
2. **Windguru API + data layer** — port the networking client and the
   forecast/spot/model types (`Sources/Forescoop/Classes/Model`,
   `Sources/Forescoop/Classes/Service` on the Swift side) to Kotlin. No UI
   beyond a debug screen.
3. **First real screen** — a single-location forecast view (roughly
   `ForecastOverview.swift`'s scope): current conditions, hourly strip.
4. **Grid view** — the hour × metric comparison table
   (`WindguruForecastGridView.swift`).
5. **Settings** — units, theme (System/Light/Dark + named palettes),
   language override, forecast view mode.
6. **Widgets** — Glance-based home screen widget, Android's equivalent of
   the WidgetKit extension (`Sources/ForescoopWidgets`).
7. **Forecast insight** — the AI-assisted "Overview" write-up
   (`ForecastIntelligenceText.swift`'s deterministic + on-device-model
   paths) — Android equivalent likely uses Gemini Nano / ML Kit GenAI APIs
   where available, falling back to the same deterministic template logic.
8. Graph view, alerts, favorites/account, localization, CarPlay-equivalent
   (Android Auto) — later, roughly mirroring the Swift app's feature order.

Each step should land as its own reviewable change, matching how the Swift
side of this session progressed feature-by-feature rather than as one large
port.
