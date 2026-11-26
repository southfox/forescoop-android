# Ventus for Android

Android port of the Ventus/Forescoop weather app (the Swift/SwiftUI original
lives at https://github.com/southfox/ventus). This is a **separate project** in its own
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

## Build and checks

Open the project in Android Studio or install JDK 17 and Android SDK 35, then run:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The checked-in Gradle wrapper downloads the configured Gradle version. CI runs
unit tests and lint on pull requests targeting `dev` or `main`, pushes to those
branches, and manual runs. The lint baseline records existing findings; new lint
errors fail CI. Reduce the baseline as existing issues are fixed. Instrumented
tests need an emulator or device and are not currently run in CI.

## Download a test build

After checks pass, the **Android CI** workflow builds a debug APK. Open the
workflow run on GitHub Actions and download the `ventus-android-debug-apk`
artifact from its summary, unzip it, and install the APK on an Android device
(Android 8.0 or newer). GitHub sign-in and repository read access are required;
artifacts expire after 14 days. Standard GitHub-hosted runners are free for
public repositories. Reports are also retained for 14 days.

These builds use a temporary debug signing key. Different runs may have different
keys, so installing an update can require uninstalling the previous build and
losing its local data. They are for testing, not Play Store distribution.

For a TestFlight-like tester experience, [Firebase App Distribution](https://firebase.google.com/products/app-distribution)
is a no-cost option with tester invitations and a web console. Create a Firebase
project and register `org.forescoop.ventus`, establish a securely stored stable
signing key, and configure authenticated CI uploads and tester groups before
automating distribution. Until then the Actions APK is the available web download.

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
