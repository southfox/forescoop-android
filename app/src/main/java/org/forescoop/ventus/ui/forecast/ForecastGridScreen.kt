//
//  ForecastGridScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.data.model.Forecast
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.common.Knots
import org.forescoop.ventus.ui.common.Temperature
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WeatherColorScale
import org.forescoop.ventus.ui.common.WindSpeedUnit

import org.forescoop.ventus.ui.common.*

@Composable
fun ForecastGridScreen(
    spotForecast: SpotForecast,
    viewModel: ForecastGridViewModel = viewModel(),
    onNavigateToDashboard: () -> Unit,
    onNavigateToGrid: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onNavigateToOverview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean,
    isAuthenticated: Boolean,
    onNavigateBack: () -> Unit
) {
    val hours = spotForecast.availableForecastHours
    val windUnit by viewModel.windUnit.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VentusTopBar(
                currentRoute = "grid",
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToGrid = onNavigateToGrid,
                onNavigateToGraph = onNavigateToGraph,
                onNavigateToOverview = onNavigateToOverview,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToProfile = onNavigateToProfile,
                onLogout = onLogout,
                onToggleFavorite = onToggleFavorite,
                isFavorite = isFavorite,
                isAuthenticated = isAuthenticated
            )
            
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = spotForecast.displayName,
                    style = MaterialTheme.typography.titleLarge
                )
                if (!spotForecast.spot.isNullOrEmpty()) {
                    Text(
                        text = spotForecast.spot!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxSize()) {
                // Row Labels
                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    GridLabel("Hour", isHeader = true)
                    GridLabel("Wind (${windUnit.label})")
                    GridLabel("Gust (${windUnit.label})")
                    GridLabel("Dir")
                    GridLabel("Temp (${tempUnit.label})")
                    GridLabel("Cloud (%)")
                    GridLabel("Precip (mm)")
                }

                // Forecast Data
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Row {
                        hours.forEach { hour ->
                            ForecastColumn(hour, spotForecast, windUnit, tempUnit)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridLabel(text: String, isHeader: Boolean = false) {
    Box(
        modifier = Modifier
            .height(if (isHeader) 45.dp else 30.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForecastColumn(
    hour: String,
    spotForecast: SpotForecast,
    windUnit: WindSpeedUnit,
    tempUnit: TemperatureUnit
) {
    val forecast = spotForecast.forecast ?: return
    val currentHour = spotForecast.currentForecastHour
    val date = spotForecast.forecastDate(hour)
    val showDay = ForecastDateFormatter.shouldShowDay(hour, currentHour)
    
    Column(modifier = Modifier.width(60.dp)) {
        GridHeaderCell(
            day = if (showDay) ForecastDateFormatter.formatDay(date) else null,
            hour = ForecastDateFormatter.formatHour(LocalContext.current, date)
        )
        
        val rawWind = forecast.windSpeed(hour)
        val windValue = rawWind?.let { Knots(it).value(windUnit) }
        DataCell(
            text = windValue?.toInt()?.toString() ?: "--",
            backgroundColor = rawWind?.let { WeatherColorScale.windColor(it) } ?: Color.Transparent
        )
        
        val rawGust = forecast.windGustsKnots(hour)
        val gustValue = rawGust?.let { Knots(it).value(windUnit) }
        DataCell(
            text = gustValue?.toInt()?.toString() ?: "--",
            backgroundColor = rawGust?.let { WeatherColorScale.gustColor(it) } ?: Color.Transparent
        )
        
        DataCell(text = forecast.windDirectionName(hour) ?: "--")
        
        val rawTemp = forecast.temperatureReal(hour) ?: forecast.temperature(hour)
        val tempValue = rawTemp?.let { Temperature(it).value(tempUnit) }
        DataCell(
            text = tempValue?.toInt()?.toString() ?: "--",
            backgroundColor = rawTemp?.let { WeatherColorScale.temperatureColor(it) } ?: Color.Transparent
        )
        
        val cloud = forecast.cloudCoverTotal(hour)
        DataCell(
            text = cloud?.toString() ?: "--",
            backgroundColor = cloud?.let { WeatherColorScale.cloudColor(it) } ?: Color.Transparent
        )
        
        val precip = forecast.precipitation(hour) ?: forecast.precipitation1(hour)
        DataCell(
            text = precip?.toString() ?: "--",
            backgroundColor = precip?.let { WeatherColorScale.precipitationColor(it) } ?: Color.Transparent,
            textColor = if (precip != null && precip > 0 && (forecast.temperatureReal(hour) ?: 0.0) <= 0) Color.Blue else Color.Unspecified
        )
    }
}

@Composable
fun GridHeaderCell(day: String?, hour: String) {
    Box(
        modifier = Modifier
            .height(45.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day ?: " ",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                maxLines = 1
            )
            Text(
                text = hour,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DataCell(
    text: String,
    isHeader: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    textColor: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth()
            .background(if (isHeader) MaterialTheme.colorScheme.secondaryContainer else backgroundColor)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (isHeader) 12.sp else 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) MaterialTheme.colorScheme.onSecondaryContainer else textColor
        )
    }
}
