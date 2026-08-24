//
//  ForecastGridScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.R
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.common.*
import java.util.Date
import java.util.Locale

sealed class GridHour {
    data class Forecast(val hour: String) : GridHour()
    data class SunEvent(val symbolName: String, val date: Date, val previousHour: String, val nextHour: String) : GridHour()

    val id: String get() = when(this) {
        is Forecast -> hour
        is SunEvent -> "sun:$symbolName:${date.time}"
    }
}

@Composable
fun ForecastGridScreen(
    spotForecast: SpotForecast,
    viewModel: ForecastGridViewModel = viewModel(),
    onNavigateToDashboard: () -> Unit,
    onNavigateToGrid: () -> Unit,
    onNavigateToGraph: () -> Unit,
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
    val hours = remember(spotForecast) {
        val baseHours = spotForecast.availableForecastHours
        spotForecast.insertingDaylightEvents<GridHour>(
            items = baseHours.map { GridHour.Forecast(it) },
            dateProvider = { 
                when(it) {
                    is GridHour.Forecast -> spotForecast.forecastDate(it.hour) ?: Date(0)
                    is GridHour.SunEvent -> it.date
                }
            },
            makeEvent = { prev, next, date, symbol ->
                GridHour.SunEvent(symbol, date, (prev as GridHour.Forecast).hour, (next as GridHour.Forecast).hour)
            }
        )
    }
    val windUnit by viewModel.windUnit.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val precipUnit by viewModel.precipitationUnit.collectAsState()
    val pressureUnit by viewModel.pressureUnit.collectAsState()
    val freezingLevelUnit by viewModel.freezingLevelUnit.collectAsState()
    val waveUnit by viewModel.waveHeightUnit.collectAsState()
    val listState = rememberLazyListState()
    
    val hasWaveData = remember(spotForecast) {
        spotForecast.forecast?.let { it.waveHeight("0") != null } ?: false
    }

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
                // Row Labels (Fixed)
                Column(
                    modifier = Modifier
                        .width(150.dp) // Adjusted width for icons + text
                        .verticalScroll(rememberScrollState())
                ) {
                    GridLabel("Hour", isHeader = true)
                    GridLabel("Wind speed", icon = Icons.Outlined.Air, unit = windUnit.label, onClick = { viewModel.toggleWindUnit() })
                    GridLabel("Wind gusts", icon = Icons.Outlined.WindPower, unit = windUnit.label, onClick = { viewModel.toggleWindUnit() })
                    GridLabel("Wind direction", icon = Icons.Outlined.Explore)
                    GridLabel("Temperature", icon = Icons.Outlined.DeviceThermostat, unit = tempUnit.label, onClick = { viewModel.toggleTempUnit() })
                    GridLabel("Real temperature", icon = Icons.Outlined.DeviceThermostat, unit = tempUnit.label, onClick = { viewModel.toggleTempUnit() })
                    GridLabel("Wind chill", icon = Icons.Outlined.AcUnit, unit = tempUnit.label, onClick = { viewModel.toggleTempUnit() })
                    GridLabel("Freezing level", icon = Icons.Outlined.Landscape, unit = freezingLevelUnit.label, onClick = { viewModel.toggleFreezingLevelUnit() })
                    GridLabel("Cloud cover", icon = Icons.Outlined.Cloud, unit = "%")
                    GridLabel("Precipitation", icon = Icons.Outlined.BeachAccess, unit = precipUnit.label, onClick = { viewModel.togglePrecipUnit() })
                    GridLabel("Pressure", icon = Icons.Outlined.Compress, unit = pressureUnit.label, onClick = { viewModel.togglePressureUnit() })
                    GridLabel("Humidity", icon = Icons.Outlined.WaterDrop, unit = "%")
                    if (hasWaveData) {
                        GridLabel("Wave height", icon = Icons.Outlined.Water, unit = waveUnit.label, onClick = { viewModel.toggleWaveUnit() })
                        GridLabel("Wave period", icon = Icons.Outlined.Timer, unit = "s")
                        GridLabel("Wave direction", icon = Icons.Outlined.Explore)
                    }
                }

                // Forecast Data (LazyRow)
                LazyRow(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(hours, key = { it.id }) { hour ->
                        ForecastColumn(hour, spotForecast, windUnit, tempUnit, precipUnit, pressureUnit, freezingLevelUnit, waveUnit)
                    }
                }
            }
        }
    }
}

@Composable
fun GridLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, unit: String? = null, isHeader: Boolean = false, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .height(if (isHeader) 85.dp else 30.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Column {
                Text(
                    text = text, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (unit != null) {
                    Text(
                        text = "($unit)", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontSize = 8.sp, 
                        color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastColumn(
    gridHour: GridHour,
    spotForecast: SpotForecast,
    windUnit: WindSpeedUnit,
    tempUnit: TemperatureUnit,
    precipUnit: PrecipitationUnit,
    pressureUnit: PressureUnit,
    freezingLevelUnit: FreezingLevelUnit,
    waveUnit: WaveHeightUnit
) {
    val forecast = spotForecast.forecast ?: return
    val date = when (gridHour) {
        is GridHour.Forecast -> spotForecast.forecastDate(gridHour.hour)
        is GridHour.SunEvent -> gridHour.date
    }
    val showDay = ForecastDateFormatter.shouldShowDay(date, spotForecast.tz)
    val hourLabel = ForecastDateFormatter.formatHour(LocalContext.current, date, spotForecast.tz)
    
    val hasWaveData = forecast.waveHeight("0") != null

    Column(modifier = Modifier.width(60.dp)) {
        val symbol = when (gridHour) {
            is GridHour.SunEvent -> gridHour.symbolName
            is GridHour.Forecast -> spotForecast.weatherSymbolName(gridHour.hour)
        }
        
        GridHeaderCell(
            day = if (showDay) ForecastDateFormatter.formatDay(date, spotForecast.tz) else null,
            hour = hourLabel,
            symbol = symbol
        )
        
        // Wind speed
        val rawWind = when(gridHour) {
            is GridHour.Forecast -> forecast.windSpeed(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.windSpeed(gridHour.previousHour)
                val b = forecast.windSpeed(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(
            text = rawWind?.let { Knots(it).value(windUnit).toInt().toString() } ?: "--",
            backgroundColor = rawWind?.let { WeatherColorScale.windColor(it) } ?: Color.Transparent
        )
        
        // Wind gusts
        val rawGust = when(gridHour) {
            is GridHour.Forecast -> forecast.windGustsKnots(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.windGustsKnots(gridHour.previousHour)
                val b = forecast.windGustsKnots(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(
            text = rawGust?.let { Knots(it).value(windUnit).toInt().toString() } ?: "--",
            backgroundColor = rawGust?.let { WeatherColorScale.gustColor(it) } ?: Color.Transparent
        )
        
        // Wind direction
        val dirDegrees = when(gridHour) {
            is GridHour.Forecast -> forecast.windDirection(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.windDirection(gridHour.previousHour)
                val b = forecast.windDirection(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(
            text = dirDegrees?.let { 
                // Convert wind direction (from) to travel direction (to) by adding 180 as in iOS
                val travel = (it + 180.0) % 360.0
                val arrows = listOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")
                arrows[( ((travel + 22.5) / 45.0).toInt() % 8 )]
            } ?: "--",
            isDirection = true
        )
        
        // Temperature
        val rawTemp = when(gridHour) {
            is GridHour.Forecast -> forecast.temperature(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.temperature(gridHour.previousHour)
                val b = forecast.temperature(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(
            text = rawTemp?.let { Temperature(it).value(tempUnit).toInt().toString() } ?: "--",
            backgroundColor = rawTemp?.let { WeatherColorScale.temperatureColor(it) } ?: Color.Transparent
        )

        // Real Temperature
        val rawTempReal = when(gridHour) {
            is GridHour.Forecast -> forecast.temperatureReal(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.temperatureReal(gridHour.previousHour)
                val b = forecast.temperatureReal(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(
            text = rawTempReal?.let { Temperature(it).value(tempUnit).toInt().toString() } ?: "--",
            backgroundColor = rawTempReal?.let { WeatherColorScale.temperatureColor(it) } ?: Color.Transparent
        )

        // Wind Chill
        val rawChill = when(gridHour) {
            is GridHour.Forecast -> forecast.windChill(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.windChill(gridHour.previousHour)
                val b = forecast.windChill(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(
            text = rawChill?.let { Temperature(it).value(tempUnit).toInt().toString() } ?: "--",
            backgroundColor = rawChill?.let { WeatherColorScale.temperatureColor(it) } ?: Color.Transparent
        )

        // Freezing Level
        val rawFreezing = when(gridHour) {
            is GridHour.Forecast -> forecast.freezingLevelHeightInMeters(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.freezingLevelHeightInMeters(gridHour.previousHour)
                val b = forecast.freezingLevelHeightInMeters(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(text = rawFreezing?.let { FreezingLevel(it).value(freezingLevelUnit).toInt().toString() } ?: "--")

        // Cloud cover
        val cloud = when(gridHour) {
            is GridHour.Forecast -> forecast.cloudCoverTotal(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.cloudCoverTotal(gridHour.previousHour) ?: 0
                val b = forecast.cloudCoverTotal(gridHour.nextHour) ?: 0
                (a + b) / 2
            }
        }
        DataCell(
            text = cloud?.toString() ?: "--",
            backgroundColor = cloud?.let { WeatherColorScale.cloudColor(it) } ?: Color.Transparent
        )
        
        // Precipitation
        val rawPrecip = when(gridHour) {
            is GridHour.Forecast -> forecast.precipitation(gridHour.hour) ?: forecast.precipitation1(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.precipitation(gridHour.previousHour) ?: forecast.precipitation1(gridHour.previousHour) ?: 0.0
                val b = forecast.precipitation(gridHour.nextHour) ?: forecast.precipitation1(gridHour.nextHour) ?: 0.0
                (a + b) / 2.0
            }
        }
        DataCell(
            text = rawPrecip?.let { Precipitation(it).value(precipUnit).let { v -> if (v < 1 && v > 0) String.format(Locale.getDefault(), "%.1f", v) else v.toInt().toString() } } ?: "--",
            backgroundColor = rawPrecip?.let { WeatherColorScale.precipitationColor(it) } ?: Color.Transparent,
            textColor = if (rawPrecip != null && rawPrecip > 0 && (rawTempReal ?: 0.0) <= 0) Color.Blue else Color.Unspecified
        )

        // Pressure
        val rawPressure = when(gridHour) {
            is GridHour.Forecast -> forecast.seaLevelPressure(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.seaLevelPressure(gridHour.previousHour)
                val b = forecast.seaLevelPressure(gridHour.nextHour)
                if (a != null && b != null) (a + b) / 2.0 else a ?: b
            }
        }
        DataCell(text = rawPressure?.let { AtmosphericPressure(it).value(pressureUnit).toInt().toString() } ?: "--")

        // Humidity
        val rawHum = when(gridHour) {
            is GridHour.Forecast -> forecast.relativeHumidity(gridHour.hour)
            is GridHour.SunEvent -> {
                val a = forecast.relativeHumidity(gridHour.previousHour) ?: 0
                val b = forecast.relativeHumidity(gridHour.nextHour) ?: 0
                (a + b) / 2
            }
        }
        DataCell(text = rawHum?.toString() ?: "--")

        if (hasWaveData) {
            // Wave Height
            val rawWaveH = when(gridHour) {
                is GridHour.Forecast -> forecast.waveHeight(gridHour.hour)
                is GridHour.SunEvent -> {
                    val a = forecast.waveHeight(gridHour.previousHour)
                    val b = forecast.waveHeight(gridHour.nextHour)
                    if (a != null && b != null) (a + b) / 2.0 else a ?: b
                }
            }
            DataCell(text = rawWaveH?.let { WaveHeight(it).value(waveUnit).let { v -> String.format(Locale.getDefault(), "%.1f", v) } } ?: "--")

            // Wave Period
            val rawWaveP = when(gridHour) {
                is GridHour.Forecast -> forecast.wavePeriod(gridHour.hour)
                is GridHour.SunEvent -> {
                    val a = forecast.wavePeriod(gridHour.previousHour)
                    val b = forecast.wavePeriod(gridHour.nextHour)
                    if (a != null && b != null) (a + b) / 2.0 else a ?: b
                }
            }
            DataCell(text = rawWaveP?.let { String.format(Locale.getDefault(), "%.0f", it) } ?: "--")

            // Wave Direction
            val rawWaveD = when(gridHour) {
                is GridHour.Forecast -> forecast.waveDirection(gridHour.hour)
                is GridHour.SunEvent -> {
                    val a = forecast.waveDirection(gridHour.previousHour)
                    val b = forecast.waveDirection(gridHour.nextHour)
                    if (a != null && b != null) (a + b) / 2.0 else a ?: b
                }
            }
            DataCell(text = rawWaveD?.let { 
                val arrows = listOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")
                arrows[( ((it + 22.5) / 45.0).toInt() % 8 )]
            } ?: "--")
        }
    }
}

@Composable
fun GridHeaderCell(day: String?, hour: String, symbol: String? = null) {
    Box(
        modifier = Modifier
            .height(85.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(vertical = 4.dp)
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
            
            if (symbol != null) {
                if (symbol == "sunrise" || symbol == "sunset") {
                    Icon(
                        painter = painterResource(id = if (symbol == "sunrise") R.drawable.ic_sunrise else R.drawable.ic_sunset),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    val icon = when (symbol) {
                        "wb_sunny" -> Icons.Outlined.WbSunny
                        "wb_cloudy" -> Icons.Outlined.Cloud
                        "nightlight" -> Icons.Outlined.Nightlight
                        "cloud" -> Icons.Outlined.Cloud
                        "ac_unit" -> Icons.Outlined.AcUnit
                        "umbrella" -> Icons.Outlined.BeachAccess
                        "water_drop" -> Icons.Outlined.WaterDrop
                        else -> Icons.Outlined.WbSunny
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun DataCell(
    text: String,
    isHeader: Boolean = false,
    isDirection: Boolean = false,
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
            fontSize = if (isDirection) 14.sp else if (isHeader) 12.sp else 11.sp,
            fontWeight = if (isHeader || isDirection) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) MaterialTheme.colorScheme.onSecondaryContainer else textColor
        )
    }
}
