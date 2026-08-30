//
//  ForecastGridViewModel.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.forecast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.ui.common.*

class ForecastGridViewModel @JvmOverloads constructor(application: Application) : AndroidViewModel(application) {
    private val preferenceStore = PreferenceStore(application)

    val windUnit: StateFlow<WindSpeedUnit> = preferenceStore.getString(PreferenceStore.KEY_WIND_UNIT)
        .map { WindSpeedUnit.fromWindguruPreference(it) ?: WindSpeedUnit.KNOTS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WindSpeedUnit.KNOTS)

    val tempUnit: StateFlow<TemperatureUnit> = preferenceStore.getString(PreferenceStore.KEY_TEMP_UNIT)
        .map { TemperatureUnit.fromWindguruPreference(it) ?: TemperatureUnit.CELSIUS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TemperatureUnit.CELSIUS)

    val precipitationUnit: StateFlow<PrecipitationUnit> = preferenceStore.getString(PreferenceStore.KEY_PRECIP_UNIT)
        .map { if (it == "in") PrecipitationUnit.INCHES else PrecipitationUnit.MILLIMETERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrecipitationUnit.MILLIMETERS)

    val pressureUnit: StateFlow<PressureUnit> = preferenceStore.getString(PreferenceStore.KEY_PRESSURE_UNIT)
        .map { 
            when(it) {
                "mbar" -> PressureUnit.MILLIBARS
                "inHg" -> PressureUnit.INCHES_OF_MERCURY
                "mmHg" -> PressureUnit.MILLIMETERS_OF_MERCURY
                else -> PressureUnit.HECTOPASCALS
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PressureUnit.HECTOPASCALS)

    val freezingLevelUnit: StateFlow<FreezingLevelUnit> = preferenceStore.getString(PreferenceStore.KEY_FREEZING_LEVEL_UNIT)
        .map { if (it == "ft") FreezingLevelUnit.FEET else FreezingLevelUnit.METERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreezingLevelUnit.METERS)

    val waveHeightUnit: StateFlow<WaveHeightUnit> = preferenceStore.getString(PreferenceStore.KEY_WAVE_HEIGHT_UNIT)
        .map { if (it == "ft") WaveHeightUnit.FEET else WaveHeightUnit.METERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaveHeightUnit.METERS)

    val isLabelsExpanded: StateFlow<Boolean> = preferenceStore.getBoolean(PreferenceStore.KEY_EXPAND_LABELS, true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

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

    fun toggleLabelsExpanded() {
        viewModelScope.launch {
            preferenceStore.saveBoolean(PreferenceStore.KEY_EXPAND_LABELS, !isLabelsExpanded.value)
        }
    }
}
