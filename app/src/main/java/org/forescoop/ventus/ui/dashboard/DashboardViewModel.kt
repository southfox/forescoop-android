//
//  DashboardViewModel.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.domain.repository.WindguruRepository

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val spotForecast: SpotForecast) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val repository: WindguruRepository = WindguruRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadForecast("64141") // Default spot from README
    }

    fun loadForecast(spotId: String) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val forecast = repository.getForecast(spotId)
                _uiState.value = DashboardUiState.Success(forecast)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
