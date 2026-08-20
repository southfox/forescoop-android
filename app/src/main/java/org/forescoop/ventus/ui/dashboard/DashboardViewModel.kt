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
import kotlinx.coroutines.launch
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val spotForecast: SpotForecast,
        val windUnit: WindSpeedUnit,
        val tempUnit: TemperatureUnit
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: WindguruRepository = WindguruRepository()
) : AndroidViewModel(application) {

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

    val uiState: StateFlow<DashboardUiState> = combine(
        _spotForecast, windUnit, tempUnit, _error, _loading
    ) { forecast, wind, temp, error, loading ->
        when {
            error != null -> DashboardUiState.Error(error)
            loading || forecast == null -> DashboardUiState.Loading
            else -> DashboardUiState.Success(forecast, wind, temp)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState.Loading)

    init {
        loadForecast("64141") // Default spot from README
    }

    fun loadForecast(spotId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val forecast = repository.getForecast(spotId)
                _spotForecast.value = forecast
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _loading.value = false
            }
        }
    }
}
