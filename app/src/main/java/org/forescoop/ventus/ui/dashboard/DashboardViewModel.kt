//
//  DashboardViewModel.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.ui.common.*
import org.forescoop.ventus.analytics.AnalyticsEvent
import org.forescoop.ventus.analytics.AnalyticsLogging
import org.forescoop.ventus.analytics.log
import org.forescoop.ventus.util.LocationTracker

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val spotForecast: SpotForecast,
        val windUnit: WindSpeedUnit,
        val tempUnit: TemperatureUnit,
        val precipitationUnit: PrecipitationUnit,
        val pressureUnit: PressureUnit,
        val freezingLevelUnit: FreezingLevelUnit,
        val waveHeightUnit: WaveHeightUnit,
        val isFavorite: Boolean,
        val summary: String,
        val isAuthenticated: Boolean,
        val selectedHour: String?
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    application: Application,
    private val analytics: AnalyticsLogging,
    private val repository: WindguruRepository,
    private val locationTracker: LocationTracker
) : AndroidViewModel(application) {

    private val accountManager = (application as VentusApplication).accountManager
    private val preferenceStore = PreferenceStore(application)
    private val _spotForecast = MutableStateFlow<SpotForecast?>(null)
    private val _selectedHour = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)
    private val _loading = MutableStateFlow(true)

    val windUnit = preferenceStore.getString(PreferenceStore.KEY_WIND_UNIT)
        .map { WindSpeedUnit.fromWindguruPreference(it) ?: WindSpeedUnit.KNOTS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WindSpeedUnit.KNOTS)

    val tempUnit = preferenceStore.getString(PreferenceStore.KEY_TEMP_UNIT)
        .map { TemperatureUnit.fromWindguruPreference(it) ?: TemperatureUnit.CELSIUS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TemperatureUnit.CELSIUS)

    val favorites = preferenceStore.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isAuthenticated = accountManager.currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val precipitationUnit = preferenceStore.getString(PreferenceStore.KEY_PRECIP_UNIT)
        .map { if (it == "in") PrecipitationUnit.INCHES else PrecipitationUnit.MILLIMETERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrecipitationUnit.MILLIMETERS)

    val pressureUnit = preferenceStore.getString(PreferenceStore.KEY_PRESSURE_UNIT)
        .map { 
            when(it) {
                "mbar" -> PressureUnit.MILLIBARS
                "inHg" -> PressureUnit.INCHES_OF_MERCURY
                "mmHg" -> PressureUnit.MILLIMETERS_OF_MERCURY
                else -> PressureUnit.HECTOPASCALS
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PressureUnit.HECTOPASCALS)

    val freezingLevelUnit = preferenceStore.getString(PreferenceStore.KEY_FREEZING_LEVEL_UNIT)
        .map { if (it == "ft") FreezingLevelUnit.FEET else FreezingLevelUnit.METERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreezingLevelUnit.METERS)

    val waveHeightUnit = preferenceStore.getString(PreferenceStore.KEY_WAVE_HEIGHT_UNIT)
        .map { if (it == "ft") WaveHeightUnit.FEET else WaveHeightUnit.METERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaveHeightUnit.METERS)

    val uiState: StateFlow<DashboardUiState> = combine(
        _spotForecast, _selectedHour, windUnit, tempUnit, 
        precipitationUnit, pressureUnit, freezingLevelUnit, waveHeightUnit,
        favorites, isAuthenticated, _error, _loading
    ) { args ->
        val forecast = args[0] as? SpotForecast
        val hourOverride = args[1] as? String
        val wind = args[2] as WindSpeedUnit
        val temp = args[3] as TemperatureUnit
        val precip = args[4] as PrecipitationUnit
        val pressure = args[5] as PressureUnit
        val freezing = args[6] as FreezingLevelUnit
        val wave = args[7] as WaveHeightUnit
        val favs = args[8] as Set<String>
        val auth = args[9] as Boolean
        val error = args[10] as? String
        val loading = args[11] as Boolean

        when {
            error != null -> DashboardUiState.Error(error)
            loading || forecast == null -> DashboardUiState.Loading
            else -> {
                val hour = hourOverride ?: forecast.currentForecastHour
                val summary = ForecastIntelligence.detailedSummary(
                    context = getApplication(),
                    forecast = forecast,
                    hour = hour,
                    prefs = ForecastUnitPreferences(temperatureUnit = temp, windSpeedUnit = wind)
                )
                DashboardUiState.Success(
                    spotForecast = forecast,
                    windUnit = wind,
                    tempUnit = temp,
                    precipitationUnit = precip,
                    pressureUnit = pressure,
                    freezingLevelUnit = freezing,
                    waveHeightUnit = wave,
                    isFavorite = favs.contains(forecast.idSpot),
                    summary = summary,
                    isAuthenticated = auth,
                    selectedHour = hour
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState.Loading)

    init {
        viewModelScope.launch {
            accountManager.initialize()
            
            // Reload forecast whenever auth state changes to ensure we use the right endpoint (PRO vs Public)
            accountManager.currentUser.collect { user ->
                val lastId = preferenceStore.getString("last_spot_id").first()
                loadForecast(lastId ?: "64141")
            }
        }
    }

    fun loadForecast(spotId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _selectedHour.value = null // Reset selected hour for new spot
            try {
                val username = accountManager.getUsername()
                val password = accountManager.getPassword()()
                
                val forecast = if (!username.isNullOrEmpty() && password != null) {
                    try {
                        val result = repository.getWForecast(spotId, username = username, password = password)
                        // Verify if we actually got forecast data, otherwise fallback
                        if (result.forecasts.isNotEmpty()) {
                            result
                        } else {
                            repository.getForecast(spotId)
                        }
                    } catch (e: Exception) {
                        repository.getForecast(spotId)
                    }
                } else {
                    repository.getForecast(spotId)
                }
                
                _spotForecast.value = forecast
                preferenceStore.saveString("last_spot_id", spotId)
                analytics.log(AnalyticsEvent.ForecastLoadSucceeded(spotId))
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                analytics.log(AnalyticsEvent.ForecastLoadFailed(spotId, e.message ?: e.toString()))
                analytics.recordException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadForecastForCurrentLocation() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _selectedHour.value = null
            try {
                val location = locationTracker.getCurrentLocation()
                if (location == null) {
                    _error.value = "Could not retrieve current location"
                    _loading.value = false
                    return@launch
                }

                val username = accountManager.getUsername()
                val password = accountManager.getPassword()()

                if (username.isNullOrEmpty() || password == null) {
                    _error.value = "Current location forecasts are only available for PRO users. Please log in."
                    _loading.value = false
                    return@launch
                }

                val forecast = repository.getWForecastLatLon(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    username = username,
                    password = password
                )

                _spotForecast.value = forecast
                // We don't save "last_spot_id" for current location as it's dynamic
                analytics.log(AnalyticsEvent.ForecastLoadSucceeded("current_location"))
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                analytics.log(AnalyticsEvent.ForecastLoadFailed("current_location", e.message ?: e.toString()))
                analytics.recordException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleFavorite() {
        val currentForecast = _spotForecast.value ?: return
        val spotId = currentForecast.idSpot ?: return
        viewModelScope.launch {
            val willBeFavorite = !favorites.value.contains(spotId)
            if (willBeFavorite) {
                preferenceStore.addFavorite(spotId)
            } else {
                preferenceStore.removeFavorite(spotId)
            }
            analytics.log(AnalyticsEvent.FavoriteToggled(spotId, willBeFavorite))
        }
    }

    fun selectHour(hour: String) {
        _selectedHour.value = hour
    }

    fun nextHour() {
        val forecast = _spotForecast.value ?: return
        val hours = forecast.availableForecastHours
        val current = _selectedHour.value ?: forecast.currentForecastHour ?: return
        val index = hours.indexOf(current)
        if (index != -1 && index < hours.size - 1) {
            _selectedHour.value = hours[index + 1]
        }
    }

    fun previousHour() {
        val forecast = _spotForecast.value ?: return
        val hours = forecast.availableForecastHours
        val current = _selectedHour.value ?: forecast.currentForecastHour ?: return
        val index = hours.indexOf(current)
        if (index > 0) {
            _selectedHour.value = hours[index - 1]
        }
    }

    fun toggleWindUnit() {
        viewModelScope.launch {
            val next = windUnit.value.next()
            preferenceStore.saveString(PreferenceStore.KEY_WIND_UNIT, next.name.lowercase())
        }
    }

    fun toggleTempUnit() {
        viewModelScope.launch {
            val next = tempUnit.value.next()
            preferenceStore.saveString(PreferenceStore.KEY_TEMP_UNIT, if (next == TemperatureUnit.CELSIUS) "c" else "f")
        }
    }

    fun togglePrecipUnit() {
        viewModelScope.launch {
            val next = precipitationUnit.value.next()
            preferenceStore.saveString(PreferenceStore.KEY_PRECIP_UNIT, if (next == PrecipitationUnit.INCHES) "in" else "mm")
        }
    }

    fun togglePressureUnit() {
        viewModelScope.launch {
            val next = pressureUnit.value.next()
            val prefValue = when(next) {
                PressureUnit.MILLIBARS -> "mbar"
                PressureUnit.INCHES_OF_MERCURY -> "inHg"
                PressureUnit.MILLIMETERS_OF_MERCURY -> "mmHg"
                else -> "hPa"
            }
            preferenceStore.saveString(PreferenceStore.KEY_PRESSURE_UNIT, prefValue)
        }
    }

    fun toggleFreezingLevelUnit() {
        viewModelScope.launch {
            val next = freezingLevelUnit.value.next()
            preferenceStore.saveString(PreferenceStore.KEY_FREEZING_LEVEL_UNIT, if (next == FreezingLevelUnit.FEET) "ft" else "m")
        }
    }

    fun toggleWaveUnit() {
        viewModelScope.launch {
            val next = waveHeightUnit.value.next()
            preferenceStore.saveString(PreferenceStore.KEY_WAVE_HEIGHT_UNIT, if (next == WaveHeightUnit.FEET) "ft" else "m")
        }
    }

    fun logout() {
        viewModelScope.launch {
            accountManager.signOut()
        }
    }
}
