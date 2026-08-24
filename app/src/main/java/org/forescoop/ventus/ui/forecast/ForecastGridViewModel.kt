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
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.ui.common.FreezingLevelUnit
import org.forescoop.ventus.ui.common.PrecipitationUnit
import org.forescoop.ventus.ui.common.PressureUnit
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WaveHeightUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit

class ForecastGridViewModel @JvmOverloads constructor(application: Application) : AndroidViewModel(application) {
    private val preferenceStore = PreferenceStore(application)

    val windUnit: StateFlow<WindSpeedUnit> = preferenceStore.getString(PreferenceStore.KEY_WIND_UNIT)
        .map { WindSpeedUnit.fromWindguruPreference(it) ?: WindSpeedUnit.KNOTS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WindSpeedUnit.KNOTS)

    val tempUnit: StateFlow<TemperatureUnit> = preferenceStore.getString(PreferenceStore.KEY_TEMP_UNIT)
        .map { TemperatureUnit.fromWindguruPreference(it) ?: TemperatureUnit.CELSIUS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TemperatureUnit.CELSIUS)

    val precipitationUnit: StateFlow<PrecipitationUnit> = preferenceStore.getString("precip_unit")
        .map { if (it == "in") PrecipitationUnit.INCHES else PrecipitationUnit.MILLIMETERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrecipitationUnit.MILLIMETERS)

    val pressureUnit: StateFlow<PressureUnit> = preferenceStore.getString("pressure_unit")
        .map { 
            when(it) {
                "mbar" -> PressureUnit.MILLIBARS
                "inHg" -> PressureUnit.INCHES_OF_MERCURY
                "mmHg" -> PressureUnit.MILLIMETERS_OF_MERCURY
                else -> PressureUnit.HECTOPASCALS
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PressureUnit.HECTOPASCALS)

    val freezingLevelUnit: StateFlow<FreezingLevelUnit> = preferenceStore.getString("freezing_level_unit")
        .map { if (it == "ft") FreezingLevelUnit.FEET else FreezingLevelUnit.METERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FreezingLevelUnit.METERS)

    val waveHeightUnit: StateFlow<WaveHeightUnit> = preferenceStore.getString("wave_unit")
        .map { if (it == "ft") WaveHeightUnit.FEET else WaveHeightUnit.METERS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaveHeightUnit.METERS)
}
