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
import org.forescoop.ventus.ui.common.ForecastIntelligence
import org.forescoop.ventus.ui.common.ForecastUnitPreferences
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit
import org.forescoop.ventus.analytics.AnalyticsEvent
import org.forescoop.ventus.analytics.AnalyticsLogging
import org.forescoop.ventus.analytics.log

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val spotForecast: SpotForecast,
        val windUnit: WindSpeedUnit,
        val tempUnit: TemperatureUnit,
        val isFavorite: Boolean,
        val summary: String,
        val isAuthenticated: Boolean
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    application: Application,
    private val analytics: AnalyticsLogging,
    private val repository: WindguruRepository
) : AndroidViewModel(application) {

    private val accountManager = (application as VentusApplication).accountManager
    private val preferenceStore = PreferenceStore(application)
    private val _spotForecast = MutableStateFlow<SpotForecast?>(null)
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

    val uiState: StateFlow<DashboardUiState> = combine(
        _spotForecast, windUnit, tempUnit, favorites, isAuthenticated, _error, _loading
    ) { args ->
        val forecast = args[0] as? SpotForecast
        val wind = args[1] as WindSpeedUnit
        val temp = args[2] as TemperatureUnit
        val favs = args[3] as Set<String>
        val auth = args[4] as Boolean
        val error = args[5] as? String
        val loading = args[6] as Boolean

        when {
            error != null -> DashboardUiState.Error(error)
            loading || forecast == null -> DashboardUiState.Loading
            else -> {
                val summary = ForecastIntelligence.detailedSummary(
                    context = getApplication(),
                    forecast = forecast,
                    hour = forecast.currentForecastHour,
                    prefs = ForecastUnitPreferences(temperatureUnit = temp, windSpeedUnit = wind)
                )
                DashboardUiState.Success(forecast, wind, temp, favs.contains(forecast.idSpot), summary, auth)
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

    fun logout() {
        viewModelScope.launch {
            accountManager.signOut()
        }
    }
}
