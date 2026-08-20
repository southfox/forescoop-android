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
import org.forescoop.ventus.ui.search.SpotSearchScreen
import org.forescoop.ventus.ui.forecast.ForecastGraphScreen
import org.forescoop.ventus.ui.login.LoginScreen

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
                            onNavigateToDashboard = {
                                if (navController.currentDestination?.route != "dashboard") {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            },
                            onNavigateToGrid = {
                                navController.navigate("grid") {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            },
                            onNavigateToSearch = {
                                navController.navigate("search")
                            },
                            onNavigateToGraph = {
                                navController.navigate("graph") {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate("login")
                            }
                        )
                    }
                    composable("grid") {
                        val state by dashboardViewModel.uiState.collectAsState()
                        if (state is DashboardUiState.Success) {
                            val success = state as DashboardUiState.Success
                            ForecastGridScreen(
                                spotForecast = success.spotForecast,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onNavigateToGrid = {},
                                onNavigateToGraph = {
                                    navController.navigate("graph") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToSearch = {
                                    navController.navigate("search")
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onToggleFavorite = { dashboardViewModel.toggleFavorite() },
                                isFavorite = success.isFavorite,
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
                    composable("search") {
                        SpotSearchScreen(
                            onSpotSelected = { spotId ->
                                dashboardViewModel.loadForecast(spotId)
                                navController.popBackStack()
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("graph") {
                        val state by dashboardViewModel.uiState.collectAsState()
                        if (state is DashboardUiState.Success) {
                            val success = state as DashboardUiState.Success
                            ForecastGraphScreen(
                                spotForecast = success.spotForecast,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onNavigateToGrid = {
                                    navController.navigate("grid") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateToGraph = {},
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToSearch = {
                                    navController.navigate("search")
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onToggleFavorite = { dashboardViewModel.toggleFavorite() },
                                isFavorite = success.isFavorite,
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
}
