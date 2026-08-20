# Ventus for Android

Android port of the Ventus/Forescoop weather app (the Swift/SwiftUI original
lives at `../forescoop`). This is a **separate project** in its own
directory/git history, started fresh rather than mixed into the Swift
package.

## Status: Data Layer + Initial UI (Step 2-4)

The project now includes the core Windguru API integration and initial UI for the Dashboard and Forecast Grid.

- **Data Layer**: Migrated Swift models to Kotlin Data Classes with custom GSON deserializers.
- **Networking**: Retrofit integration with the Windguru API.
- **UI**: 
    - **Dashboard**: Current conditions for a spot with wind and temperature.
    - **Forecast Grid**: Dense horizontal table of forecast metrics.
    - **Navigation**: Jetpack Navigation between screens.
- **Tech Stack**:
    - Kotlin + Jetpack Compose + Material3
    - Retrofit + Gson for networking
    - Jetpack Navigation
    - `minSdk` 26, `targetSdk`/`compileSdk` 35

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

1. **Bare scaffold** [x]
2. **Windguru API + data layer** [x]
3. **First real screen (Dashboard)** [x]
4. **Grid view** [/] - Initial implementation done.
5. **Weather Background** - Implement the animated weather background (wind, clouds, rain, snow).
6. **Settings & Units** - Unit selection (knots, km/h, °C, °F) and preferences persistence.
7. **Spot Search & Favorites** - Port the location picker and Windguru account integration.
8. **Widgets** - Glance-based home screen widgets.

Each step should land as its own reviewable change, matching how the Swift
side of this session progressed feature-by-feature rather than as one large
port.
