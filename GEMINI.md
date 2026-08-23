# Gemini Notes - Ventus Android Migration

This file serves as a reference for Gemini to track the project's progress and handle context resets (e.g., after `RESOURCE_EXHAUSTED` or session timeouts).

## Recent Work (Post-Resource Exhaustion)

The following improvements were made manually or in a parallel session:

- **Dependency Injection & Application**:
    - Created `VentusApplication` to manage global dependencies (`analytics`, `windguruService`).
    - Implemented `LambdaViewModelFactory` for flexible ViewModel instantiation.
    - Updated `DashboardViewModel` to use injected `WindguruRepository` and `AnalyticsLogging`.

- **Analytics Integration**:
    - Added `AnalyticsLogging` interface and `FirebaseAnalyticsLogger` implementation.

- **UI & Navigation Refinements**:
    - **Unified Top Bar**: Created `VentusTopBar` with a persistent hamburger menu across all forecast views (Overview, Grid, Graph).
    - **Navigation State**: Improved `MainActivity` navigation logic to handle Peer Views (Grid/Graph/Dashboard) as top-level destinations without a back button.
    - **Login & Profile**: Added `LoginScreen`, `LoginViewModel`, and `AccountManager` with `SecureCredentialStore` (EncryptedSharedPreferences).
    - **Custom Graph**: Refined the `ForecastGraphScreen` with smooth Bezier curves and detailed weather metrics.

- **Data Layer Improvements**:
    - Updated `WindguruRepository` to take `WindguruService` as a constructor parameter.
    - Added `login` method to `WindguruService`.
    - Implemented `User` and `CustomColor` models with custom GSON deserializers to match the complex Windguru API responses.

## Project Status

- [x] Core Data Layer (Retrofit, Repository, GSON Adapters)
- [x] Dashboard / Overview Screen
- [x] Forecast Grid Screen (Dense Metric Table)
- [x] Forecast Graph Screen (Compose Canvas)
- [x] Animated Weather Backgrounds
- [x] Forecast Intelligence (Trend Summaries)
- [x] Spot Search & Favorites (DataStore Persistence)
- [x] Home Screen Widgets (Jetpack Glance)
- [x] Authentication Layer (Login, Profile, Secure Storage, Profile Auto-Refresh)
- [x] Windguru PRO specific features (Authenticated Forecasts)
- [x] Interactive Forecast Timeline (Hour Picker, Swapping Gestures, Nav Buttons)
- [/] Analytics Integration (Firebase)
- [ ] Forecast Accessibility Enhancements
- [ ] Current Location Forecasts (PRO)

## Tech Stack Summary

- **UI**: Jetpack Compose + Material 3
- **Navigation**: Jetpack Navigation Compose
- **Widgets**: Jetpack Glance
- **Persistence**: DataStore (Preferences/Favorites), EncryptedSharedPreferences (Credentials)
- **Networking**: Retrofit + Gson
- **Analytics**: Firebase Analytics
- **Animation**: Compose Canvas (Weather backgrounds, Forecast graphs)
