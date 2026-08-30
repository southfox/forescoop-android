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
import androidx.compose.runtime.LaunchedEffect
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
import org.forescoop.ventus.ui.profile.ProfileScreen
import org.forescoop.ventus.domain.repository.WindguruRepository
import androidx.appcompat.app.AppCompatActivity
import org.forescoop.ventus.ui.common.LambdaViewModelFactory
import org.forescoop.ventus.util.LocationTracker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as VentusApplication
        val analytics = app.analytics
        val locationTracker = LocationTracker(app)
        enableEdgeToEdge()
        setContent {
            VentusTheme {
                val navController = rememberNavController()
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = LambdaViewModelFactory { 
                        DashboardViewModel(
                            app, 
                            analytics, 
                            WindguruRepository(app.windguruService),
                            locationTracker
                        ) 
                    }
                )

                LaunchedEffect(navController) {
                    navController.currentBackStackEntryFlow.collect { entry ->
                        entry.destination.route?.let { analytics.logScreenView(it) }
                    }
                }

                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateToDashboard = {},
                            onNavigateToGrid = {
                                navController.navigate("grid") {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
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
                            onNavigateToProfile = {
                                navController.navigate("profile")
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
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                },
                                onLogout = { dashboardViewModel.logout() },
                                onToggleFavorite = { dashboardViewModel.toggleFavorite() },
                                onCurrentLocationClick = { dashboardViewModel.loadForecastForCurrentLocation() },
                                isFavorite = success.isFavorite,
                                isAuthenticated = success.isAuthenticated,
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
                    composable("profile") {
                        ProfileScreen(
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
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                },
                                onLogout = { dashboardViewModel.logout() },
                                onToggleFavorite = { dashboardViewModel.toggleFavorite() },
                                onCurrentLocationClick = { dashboardViewModel.loadForecastForCurrentLocation() },
                                isFavorite = success.isFavorite,
                                isAuthenticated = success.isAuthenticated,
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
