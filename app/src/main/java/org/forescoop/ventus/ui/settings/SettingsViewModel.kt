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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.ui.common.*
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

    val gridHourStep: StateFlow<Int> = preferenceStore.getInt(PreferenceStore.KEY_GRID_HOUR_STEP, 3)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val weatherBackgroundType: StateFlow<String> = preferenceStore.getString(PreferenceStore.KEY_WEATHER_BACKGROUND_TYPE)
        .map { it ?: "Native" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Native")

    val appearanceMode: StateFlow<String> = preferenceStore.getString(PreferenceStore.KEY_APPEARANCE_MODE)
        .map { it ?: "System" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    val themeType: StateFlow<String> = preferenceStore.getString(PreferenceStore.KEY_THEME_TYPE)
        .map { it ?: "System" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    val languageCode: StateFlow<String> = preferenceStore.getString(PreferenceStore.KEY_LANGUAGE_CODE)
        .map { it ?: "System" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    val severeWeatherAlerts: StateFlow<Boolean> = preferenceStore.getBoolean(PreferenceStore.KEY_SEVERE_WEATHER_ALERTS, true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

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

    fun setGridHourStep(step: Int) {
        viewModelScope.launch {
            preferenceStore.saveInt(PreferenceStore.KEY_GRID_HOUR_STEP, step)
        }
    }

    fun setWeatherBackgroundType(type: String) {
        viewModelScope.launch {
            preferenceStore.saveString(PreferenceStore.KEY_WEATHER_BACKGROUND_TYPE, type)
        }
    }

    fun setAppearanceMode(mode: String) {
        viewModelScope.launch {
            preferenceStore.saveString(PreferenceStore.KEY_APPEARANCE_MODE, mode)
            when(mode) {
                "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    fun setThemeType(type: String) {
        viewModelScope.launch {
            preferenceStore.saveString(PreferenceStore.KEY_THEME_TYPE, type)
        }
    }

    fun setLanguageCode(code: String) {
        viewModelScope.launch {
            preferenceStore.saveString(PreferenceStore.KEY_LANGUAGE_CODE, code)
            val appLocale: LocaleListCompat = if (code == "System") {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(code)
            }
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun setSevereWeatherAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferenceStore.saveBoolean(PreferenceStore.KEY_SEVERE_WEATHER_ALERTS, enabled)
        }
    }
}
