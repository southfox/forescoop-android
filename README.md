# Ventus for Android

Android port of the Ventus/Forescoop weather app (the Swift/SwiftUI original
lives at `https://github.com/patagonia2019/forescoop`). This is a **separate project** in its own
directory/git history, started fresh rather than mixed into the Swift
package.

## Status: Spot Search & Favorites (Step 7)

The project now includes spot search and favorites management.

- **Forecast Intelligence**: Implemented a "Smart Overview" that provides human-readable summaries and highlights key trends (Ice Hazard, Cold Snap, etc.).
- **Persistence**: Favorites and the last viewed spot are persisted using Android DataStore.
- **Tech Stack**:
    - Kotlin + Jetpack Compose + Material3
    - Retrofit + Gson for networking
    - Jetpack Navigation
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
9. **Widgets** - Glance-based home screen widgets.

Each step should land as its own reviewable change, matching how the Swift
side of this session progressed feature-by-feature rather than as one large
port.
