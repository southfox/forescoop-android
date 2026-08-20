# Ventus for Android

Android port of the Ventus/Forescoop weather app (the Swift/SwiftUI original
lives at `https://github.com/patagonia2019/forescoop`). This is a **separate project** in its own
directory/git history, started fresh rather than mixed into the Swift
package.

## Status: UI Refinement + Animated Background (Step 5)

The project now includes a refined Forecast Grid and an animated weather background.

- **UI Refinement**: The Forecast Grid now features color-coded cells for wind, temperature, clouds, and precipitation, matching the Windguru look and feel.
- **Animated Background**: Implemented a native Compose-based `AnimatedWeatherBackground` that reacts to current conditions (sun, clouds, rain, snow, night).
- **Logic Enhancements**: Ported advanced weather symbol logic and current hour resolution from the iOS project.
- **Tech Stack**:
    - Kotlin + Jetpack Compose + Material3
    - Retrofit + Gson for networking
    - Jetpack Navigation
    - Compose Animations for weather effects

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
4. **Grid view** [x]
5. **Weather Background** [x]
6. **Settings & Units** - Unit selection (knots, km/h, °C, °F) and preferences persistence.
7. **Spot Search & Favorites** - Port the location picker and Windguru account integration.
8. **Widgets** - Glance-based home screen widgets.

Each step should land as its own reviewable change, matching how the Swift
side of this session progressed feature-by-feature rather than as one large
port.
