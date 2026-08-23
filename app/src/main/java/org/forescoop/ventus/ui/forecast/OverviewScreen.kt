//
//  OverviewScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-23.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.forecast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.common.*
import org.forescoop.ventus.ui.dashboard.DashboardContent
import org.forescoop.ventus.ui.dashboard.DashboardViewModel
import org.forescoop.ventus.ui.dashboard.DashboardUiState
import org.forescoop.ventus.ui.dashboard.SummarySection

@Composable
fun OverviewScreen(
    viewModel: DashboardViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToGrid: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onNavigateToOverview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DashboardUiState.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedWeatherBackground(forecast = state.spotForecast, hour = state.selectedHour)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        VentusTopBar(
                            currentRoute = "overview",
                            onNavigateToDashboard = onNavigateToDashboard,
                            onNavigateToGrid = onNavigateToGrid,
                            onNavigateToGraph = onNavigateToGraph,
                            onNavigateToOverview = onNavigateToOverview,
                            onNavigateToSettings = onNavigateToSettings,
                            onNavigateToSearch = onNavigateToSearch,
                            onNavigateToLogin = onNavigateToLogin,
                            onNavigateToProfile = onNavigateToProfile,
                            onLogout = onLogout,
                            onToggleFavorite = { viewModel.toggleFavorite() },
                            isFavorite = state.isFavorite,
                            isAuthenticated = state.isAuthenticated
                        )

                        ForecastHourSelector(
                            forecast = state.spotForecast,
                            selectedHour = state.selectedHour,
                            onHourSelected = { viewModel.selectHour(it) }
                        )
                        
                        DashboardContent(
                            spotForecast = state.spotForecast, 
                            selectedHour = state.selectedHour,
                            windUnit = state.windUnit, 
                            tempUnit = state.tempUnit, 
                            onNavigateToSearch = onNavigateToSearch,
                            onNextHour = { viewModel.nextHour() },
                            onPreviousHour = { viewModel.previousHour() }
                        )
                        
                        SummarySection(state.summary)
                    }
                }
            }
            is DashboardUiState.Error -> {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
