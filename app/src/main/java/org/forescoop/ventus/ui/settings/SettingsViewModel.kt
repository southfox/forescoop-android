//
//  SettingsViewModel.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit
import org.forescoop.ventus.analytics.AnalyticsEvent
import org.forescoop.ventus.analytics.AnalyticsLogging
import org.forescoop.ventus.analytics.log

class SettingsViewModel(application: Application, private val analytics: AnalyticsLogging) : AndroidViewModel(application) {
    private val preferenceStore = PreferenceStore(application)

    val windUnit: StateFlow<WindSpeedUnit> = preferenceStore.getString(PreferenceStore.KEY_WIND_UNIT)
        .map { WindSpeedUnit.fromWindguruPreference(it) ?: WindSpeedUnit.KNOTS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WindSpeedUnit.KNOTS)

    val tempUnit: StateFlow<TemperatureUnit> = preferenceStore.getString(PreferenceStore.KEY_TEMP_UNIT)
        .map { TemperatureUnit.fromWindguruPreference(it) ?: TemperatureUnit.CELSIUS }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TemperatureUnit.CELSIUS)

    fun setWindUnit(unit: WindSpeedUnit) {
        viewModelScope.launch {
            preferenceStore.saveString(PreferenceStore.KEY_WIND_UNIT, unit.name.lowercase())
            analytics.log(AnalyticsEvent.UnitChanged("wind_speed", unit.name.lowercase()))
        }
    }

    fun setTempUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            preferenceStore.saveString(PreferenceStore.KEY_TEMP_UNIT, unit.name.lowercase())
            analytics.log(AnalyticsEvent.UnitChanged("temperature", unit.name.lowercase()))
        }
    }
}
