# Android Migration Plan: Ventus (Windguru App)

Migrate the existing iOS SwiftUI app "Ventus" to Android using Jetpack Compose and modern Android practices. The app provides Windguru-powered weather forecasts.

## User Review Required

> [!IMPORTANT]
> The migration will focus on a clean architecture (MVVM/MVI) using Kotlin Coroutines, Retrofit for networking, and Jetpack Compose for the UI.
> We will need to map Swift models to Kotlin Data Classes.

## Proposed Changes

### 1. Data Layer (Models & Networking)
Define the core domain models and set up the Retrofit service to match the `ForecastWindguruProtocol`.

#### [NEW] [Spot.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/data/model/Spot.kt)
#### [NEW] [Forecast.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/data/model/Forecast.kt)
#### [NEW] [SpotForecast.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/data/model/SpotForecast.kt)
#### [NEW] [WindguruService.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/data/remote/WindguruService.kt)

### 2. Domain Layer (Repository)
Implement a repository that handles data fetching from remote or local sources (if needed).

#### [NEW] [WindguruRepository.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/domain/repository/WindguruRepository.kt)

### 3. UI Layer (ViewModels & Screens)
Implement ViewModels and Compose screens for the Dashboard and Forecast Grid.

#### [NEW] [DashboardViewModel.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/ui/dashboard/DashboardViewModel.kt)
#### [NEW] [DashboardScreen.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/ui/dashboard/DashboardScreen.kt)
#### [NEW] [ForecastGridScreen.kt](file:///Volumes/SamT5/code/patagonia2019/forescoop-android/app/src/main/java/org/forescoop/ventus/ui/forecast/ForecastGridScreen.kt)

### 4. Shared Components
Migrate reusable views like the weather background and unit conversion helpers.

## Verification Plan

### Automated Tests
- Unit tests for model parsing and unit conversions.
- MockWebServer tests for API integration.

### Manual Verification
- Deploy to emulator and verify data loading and UI rendering against the iOS version.
