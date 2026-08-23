//
//  DashboardScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.data.model.Forecast
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.ui.common.*

@Composable
private fun rememberDashboardViewModel(): DashboardViewModel {
    val application = LocalContext.current.applicationContext as VentusApplication
    return viewModel(factory = LambdaViewModelFactory {
        DashboardViewModel(application, application.analytics, WindguruRepository(application.windguruService))
    })
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = rememberDashboardViewModel(),
    onNavigateToDashboard: () -> Unit,
    onNavigateToGrid: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit
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
                            currentRoute = "dashboard",
                            onNavigateToDashboard = onNavigateToDashboard,
                            onNavigateToGrid = onNavigateToGrid,
                            onNavigateToGraph = onNavigateToGraph,
                            onNavigateToSettings = onNavigateToSettings,
                            onNavigateToSearch = onNavigateToSearch,
                            onNavigateToLogin = onNavigateToLogin,
                            onNavigateToProfile = onNavigateToProfile,
                            onLogout = { viewModel.logout() },
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
                        
                        WindDetailsSection(state.spotForecast, state.selectedHour, state.windUnit, state.tempUnit)
                        
                        WeatherDetailsSection(state.spotForecast, state.selectedHour)

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
    selectedHour: String?,
    windUnit: WindSpeedUnit,
    tempUnit: TemperatureUnit,
    onNavigateToSearch: () -> Unit,
    onNextHour: () -> Unit,
    onPreviousHour: () -> Unit
) {
    val forecast = spotForecast.forecast
    val hour = selectedHour ?: spotForecast.currentForecastHour
    val condition = remember(spotForecast, hour) { WeatherBackgroundCondition(spotForecast, hour) }
    val contentColor = if (condition.isNight) Color.White else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 10.dp, bottom = 24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) onPreviousHour()
                    else if (dragAmount < -50) onNextHour()
                }
            },
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onPreviousHour) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Hour", tint = contentColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            val iconName = spotForecast.weatherSymbolName(hour)
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
            
            val rawTemp = forecast?.temperatureReal(hour) ?: forecast?.temperature(hour)
            val displayTemp = rawTemp?.let { Temperature(it).value(tempUnit).toInt() }
            
            Text(
                text = "${displayTemp ?: "--"}${tempUnit.label}",
                style = MaterialTheme.typography.displayMedium,
                color = contentColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(onClick = onNextHour) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Hour", tint = contentColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wind info
        val windSpeed = forecast?.windSpeed(hour)
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
                text = forecast.windDirectionName(hour) ?: "",
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

@Composable
fun WindDetailsSection(spotForecast: SpotForecast, selectedHour: String?, windUnit: WindSpeedUnit, tempUnit: TemperatureUnit) {
    val forecast = spotForecast.forecast ?: return
    val hour = selectedHour ?: spotForecast.currentForecastHour ?: return
    
    DetailCard(title = "Wind") {
        DetailRow(
            icon = Icons.Default.Air,
            label = "Wind speed",
            value = "${Knots(forecast.windSpeed(hour) ?: 0.0).value(windUnit).toInt()} ${windUnit.label}"
        )
        DetailRow(
            icon = Icons.Default.WindPower,
            label = "Wind gusts",
            value = "${Knots(forecast.windGustsKnots(hour) ?: 0.0).value(windUnit).toInt()} ${windUnit.label}"
        )
        DetailRow(
            icon = Icons.Default.Explore,
            label = "Wind direction",
            value = forecast.windDirectionName(hour) ?: "--"
        )
        forecast.windChill(hour)?.let { chill ->
            DetailRow(
                icon = Icons.Default.AcUnit,
                label = "Wind chill",
                value = "${Temperature(chill).value(tempUnit).toInt()}${tempUnit.label}"
            )
        }
    }
}

@Composable
fun WeatherDetailsSection(spotForecast: SpotForecast, selectedHour: String?) {
    val forecast = spotForecast.forecast ?: return
    val hour = selectedHour ?: spotForecast.currentForecastHour ?: return
    
    DetailCard(title = "Weather") {
        DetailRow(
            icon = Icons.Default.Cloud,
            label = "Cloud cover",
            value = "${forecast.cloudCoverTotal(hour) ?: 0}%"
        )
        DetailRow(
            icon = Icons.Default.WaterDrop,
            label = "Humidity",
            value = "${forecast.relativeHumidity(hour) ?: 0}%"
        )
        DetailRow(
            icon = Icons.Default.Compress,
            label = "Pressure",
            value = "${forecast.seaLevelPressure(hour)?.toInt() ?: 0} hPa"
        )
        val precip = forecast.precipitation(hour) ?: forecast.precipitation1(hour) ?: 0.0
        DetailRow(
            icon = Icons.Default.BeachAccess,
            label = "Precipitation",
            value = "${precip} mm"
        )
        DetailRow(
            icon = Icons.Default.Landscape,
            label = "Freezing level",
            value = "${forecast.freezingLevelHeightInMeters(hour)?.toInt() ?: 0} m"
        )
    }
}

@Composable
fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
