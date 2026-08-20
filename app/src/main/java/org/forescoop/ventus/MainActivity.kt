//
//  MainActivity.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.forescoop.ventus.ui.dashboard.DashboardScreen
import org.forescoop.ventus.ui.forecast.ForecastGridScreen
import org.forescoop.ventus.ui.theme.VentusTheme
import org.forescoop.ventus.ui.dashboard.DashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.ui.dashboard.DashboardUiState
import org.forescoop.ventus.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VentusTheme {
                val navController = rememberNavController()
                val dashboardViewModel: DashboardViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateToGrid = {
                                navController.navigate("grid")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("grid") {
                        val state by dashboardViewModel.uiState.collectAsState()
                        if (state is DashboardUiState.Success) {
                            ForecastGridScreen(
                                spotForecast = (state as DashboardUiState.Success).spotForecast,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
