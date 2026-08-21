//
//  SpotSearchViewModel.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.data.model.SpotOwner
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.analytics.AnalyticsEvent
import org.forescoop.ventus.analytics.AnalyticsLogging
import org.forescoop.ventus.analytics.log

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val spots: List<SpotOwner>, val favorites: List<SpotOwner>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SpotSearchViewModel(
    application: Application,
    private val analytics: AnalyticsLogging,
    private val repository: WindguruRepository
) : AndroidViewModel(application) {

    private val preferenceStore = PreferenceStore(application)
    private val _searchResults = MutableStateFlow<List<SpotOwner>>(emptyList())
    private val _loading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _favoriteSpots = MutableStateFlow<List<SpotOwner>>(emptyList())

    val uiState: StateFlow<SearchUiState> = combine(
        _searchResults, _favoriteSpots, _loading, _error
    ) { results, favs, loading, error ->
        when {
            error != null -> SearchUiState.Error(error)
            loading -> SearchUiState.Loading
            results.isEmpty() && favs.isEmpty() -> SearchUiState.Idle
            else -> SearchUiState.Success(results, favs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState.Idle)

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            preferenceStore.getFavorites().collect { favIds ->
                val favSpots = favIds.map { id ->
                    val info = repository.getSpotInfo(id)
                    SpotOwner(idSpot = id, spotName = info.spotName, country = info.country)
                }
                _favoriteSpots.value = favSpots
            }
        }
    }

    fun search(query: String) {
        android.util.Log.d("SpotSearch", "Searching for: $query")
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repository.searchSpots(query)
                android.util.Log.d("SpotSearch", "Results: ${result.spots.size}")
                _searchResults.value = result.spots
                analytics.log(AnalyticsEvent.SpotSearchPerformed(query, result.spots.size))
            } catch (e: Exception) {
                android.util.Log.e("SpotSearch", "Error searching", e)
                _error.value = e.message ?: "Unknown error"
                analytics.recordException(e)
            } finally {
                _loading.value = false
            }
        }
    }
}
