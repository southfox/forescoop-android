//
//  PreferenceStore.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ventus_prefs")

class PreferenceStore(private val context: Context) {

    fun getString(key: String): Flow<String?> {
        val prefKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[prefKey]
        }
    }

    suspend fun saveString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    fun getFavorites(): Flow<Set<String>> {
        val prefKey = androidx.datastore.preferences.core.stringSetPreferencesKey(KEY_FAVORITES)
        return context.dataStore.data.map { preferences ->
            preferences[prefKey] ?: emptySet()
        }
    }

    suspend fun addFavorite(spotId: String) {
        val prefKey = androidx.datastore.preferences.core.stringSetPreferencesKey(KEY_FAVORITES)
        context.dataStore.edit { preferences ->
            val current = preferences[prefKey] ?: emptySet()
            preferences[prefKey] = current + spotId
        }
    }

    suspend fun removeFavorite(spotId: String) {
        val prefKey = androidx.datastore.preferences.core.stringSetPreferencesKey(KEY_FAVORITES)
        context.dataStore.edit { preferences ->
            val current = preferences[prefKey] ?: emptySet()
            preferences[prefKey] = current - spotId
        }
    }

    companion object {
        const val KEY_WIND_UNIT = "wind_unit"
        const val KEY_TEMP_UNIT = "temp_unit"
        const val KEY_PRECIP_UNIT = "precip_unit"
        const val KEY_PRESSURE_UNIT = "pressure_unit"
        const val KEY_FREEZING_LEVEL_UNIT = "freezing_level_unit"
        const val KEY_WAVE_HEIGHT_UNIT = "wave_unit"
        const val KEY_FAVORITES = "favorites"
    }
}
