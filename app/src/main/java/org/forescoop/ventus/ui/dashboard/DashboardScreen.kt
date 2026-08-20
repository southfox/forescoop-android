//
//  DashboardScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.common.AnimatedWeatherBackground
import org.forescoop.ventus.ui.common.Knots
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WeatherBackgroundCondition
import org.forescoop.ventus.ui.common.WindSpeedUnit

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToGrid: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToGraph: () -> Unit
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
                    AnimatedWeatherBackground(forecast = state.spotForecast)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = onNavigateToSettings,
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                ) {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                IconButton(
                                    onClick = onNavigateToSearch,
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                ) {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                IconButton(
                                    onClick = { viewModel.toggleFavorite() },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                ) {
                                    Icon(
                                        imageVector = if (state.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (state.isFavorite) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = onNavigateToGraph,
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Show Graph", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Button(
                                    onClick = onNavigateToGrid,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                ) {
                                    Text("Show Grid", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        
                        DashboardContent(state.spotForecast, state.windUnit, state.tempUnit, onNavigateToSearch)
                        
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

@Composable
fun DashboardContent(
    spotForecast: SpotForecast,
    windUnit: WindSpeedUnit,
    tempUnit: TemperatureUnit,
    onNavigateToSearch: () -> Unit
) {
    val forecast = spotForecast.forecast
    val currentHour = spotForecast.currentForecastHour
    val condition = remember(spotForecast, currentHour) { WeatherBackgroundCondition(spotForecast, currentHour) }
    val contentColor = if (condition.isNight) Color.White else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = spotForecast.displayName,
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor,
            modifier = Modifier.clickable { onNavigateToSearch() }
        )
        if (!spotForecast.spot.isNullOrEmpty()) {
            Text(
                text = spotForecast.spot!!,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
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
                "ac_unit" -> Icons.Default.AcUnit
                "umbrella" -> Icons.Default.BeachAccess
                "water_drop" -> Icons.Default.WaterDrop
                else -> Icons.Default.WbSunny
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            val rawTemp = forecast?.temperatureReal(currentHour) ?: forecast?.temperature(currentHour)
            val displayTemp = rawTemp?.let { org.forescoop.ventus.ui.common.Temperature(it).value(tempUnit).toInt() }
            
            Text(
                text = "${displayTemp ?: "--"}${tempUnit.label}",
                style = MaterialTheme.typography.displayMedium,
                color = contentColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wind info
        val windSpeed = forecast?.windSpeed(currentHour)
        if (windSpeed != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Air, contentDescription = null, tint = contentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${Knots(windSpeed).value(windUnit).toInt()} ${windUnit.label}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor
                )
            }
            Text(
                text = forecast.windDirectionName(currentHour) ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}

@Composable
fun SummarySection(summary: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Forecast Insight",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = parseMarkdown(summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}
