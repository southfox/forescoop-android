# Ventus for Android

Android port of the Ventus/Forescoop weather app (the Swift/SwiftUI original
lives at `https://github.com/patagonia2019/forescoop`). This is a **separate project** in its own
directory/git history, started fresh rather than mixed into the Swift
package.

## Status: Advanced Features (Step 8-10)

The project now includes Home Screen Widgets and a custom Graph View.

- **Home Screen Widgets**: Glance-based widgets for current weather and wind.
- **Graph View**: A dense, custom-drawn graph using Compose Canvas, showing wind, gusts, temperature, rain, humidity, and clouds.
- **Forecast Intelligence**: Implemented a "Smart Overview" that provides human-readable summaries and highlights key trends.
- **Spot Search & Favorites**: Fully functional spot discovery and local persistence.
- **Tech Stack**:
    - Kotlin + Jetpack Compose + Material3
    - Jetpack Glance for widgets
    - Compose Canvas for custom charts
    - Retrofit + Gson for networking
    - Android DataStore for local persistence

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
6. **Settings & Units** [x]
7. **Spot Search & Favorites** [x]
8. **Forecast Intelligence (AI overview)** [x]
9. **Widgets** [x]
10. **Graph View** [x]
11. **Forecast Accessibility & Refinements** - Port advanced accessibility logic and polish the UI.
12. **Windguru PRO Features** - Authenticated exact-coordinate forecasts.

Each step should land as its own reviewable change, matching how the Swift
side of this session progressed feature-by-feature rather than as one large
port.
