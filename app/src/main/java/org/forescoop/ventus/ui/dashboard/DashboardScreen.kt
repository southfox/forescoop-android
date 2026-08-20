//
//  DashboardScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.common.Knots
import org.forescoop.ventus.ui.common.WindSpeedUnit

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToGrid: () -> Unit
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
                Column {
                    Button(onClick = onNavigateToGrid, modifier = Modifier.padding(16.dp)) {
                        Text("Show Grid")
                    }
                    DashboardContent(state.spotForecast)
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

@Composable
fun DashboardContent(spotForecast: SpotForecast) {
    val forecast = spotForecast.forecast
    val currentHour = spotForecast.availableForecastHours.firstOrNull() // Simplify for now

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = spotForecast.displayName,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Basic current weather info
        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconName = spotForecast.weatherSymbolName(currentHour)
            // Mapping simple icon names to Material Icons
            val icon = when (iconName) {
                "wb_sunny" -> Icons.Default.WbSunny
                "wb_cloudy" -> Icons.Default.Cloud
                "nightlight" -> Icons.Default.Nightlight
                "cloud" -> Icons.Default.Cloud
                else -> Icons.Default.WbSunny
            }
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${forecast?.temperatureReal(currentHour) ?: forecast?.temperature(currentHour) ?: "--"}°C",
                style = MaterialTheme.typography.displayMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wind info
        val windSpeed = forecast?.windSpeed(currentHour)
        if (windSpeed != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Air, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${Knots(windSpeed).value(WindSpeedUnit.KNOTS).toInt()} knots",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Text(
                text = forecast.windDirectionName(currentHour) ?: "",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
