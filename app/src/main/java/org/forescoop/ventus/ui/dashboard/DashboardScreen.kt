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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.input.pointer.pointerInput
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.forescoop.ventus.R
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.data.model.*
import org.forescoop.ventus.data.model.precipitationAgreement
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.ui.common.*
import org.forescoop.ventus.util.LocationTracker
import java.util.Date
import java.util.Locale

@Composable
private fun rememberDashboardViewModel(): DashboardViewModel {
    val application = LocalContext.current.applicationContext as VentusApplication
    return viewModel(factory = LambdaViewModelFactory {
        DashboardViewModel(
            application, 
            application.analytics, 
            WindguruRepository(application.windguruService),
            LocationTracker(application)
        )
    })
}

@OptIn(ExperimentalPermissionsApi::class)
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
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

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
                Box(modifier = Modifier
                    .fillMaxSize()
                    .forecastAccessibilityContainer(
                        label = ForecastAccessibility.summary(LocalContext.current, state.spotForecast, state.selectedHour),
                        hint = "",
                        identifier = "dashboard_root"
                    )
                ) {
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
                            onCurrentLocationClick = {
                                if (locationPermissionState.status.isGranted) {
                                    viewModel.loadForecastForCurrentLocation()
                                } else {
                                    locationPermissionState.launchPermissionRequest()
                                }
                            },
                            isFavorite = state.isFavorite,
                            isAuthenticated = state.isAuthenticated
                        )

                        ForecastHourSelector(
                            forecast = state.spotForecast,
                            selectedHour = state.selectedHour,
                            onHourSelected = { viewModel.selectHour(it) }
                        )
                        
                        ForecastDaylightSummary(forecast = state.spotForecast)
                        
                        DashboardContent(
                            spotForecast = state.spotForecast, 
                            selectedHour = state.selectedHour,
                            windUnit = state.windUnit, 
                            tempUnit = state.tempUnit, 
                            onNavigateToSearch = onNavigateToSearch,
                            onNextHour = { viewModel.nextHour() },
                            onPreviousHour = { viewModel.previousHour() },
                            onToggleTempUnit = { viewModel.toggleTempUnit() },
                            onToggleWindUnit = { viewModel.toggleWindUnit() }
                        )

                        WindDetailsSection(
                            spotForecast = state.spotForecast, 
                            selectedHour = state.selectedHour, 
                            windUnit = state.windUnit, 
                            tempUnit = state.tempUnit,
                            onToggleWindUnit = { viewModel.toggleWindUnit() },
                            onToggleTempUnit = { viewModel.toggleTempUnit() }
                        )
                        
                        WeatherDetailsSection(
                            spotForecast = state.spotForecast, 
                            selectedHour = state.selectedHour,
                            precipUnit = state.precipitationUnit,
                            pressureUnit = state.pressureUnit,
                            freezingLevelUnit = state.freezingLevelUnit,
                            waveUnit = state.waveHeightUnit,
                            onTogglePrecipUnit = { viewModel.togglePrecipUnit() },
                            onTogglePressureUnit = { viewModel.togglePressureUnit() },
                            onToggleFreezingLevelUnit = { viewModel.toggleFreezingLevelUnit() },
                            onToggleWaveUnit = { viewModel.toggleWaveUnit() }
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

@Composable
fun DashboardContent(
    spotForecast: SpotForecast,
    selectedHour: String?,
    windUnit: WindSpeedUnit,
    tempUnit: TemperatureUnit,
    onNavigateToSearch: () -> Unit,
    onNextHour: () -> Unit,
    onPreviousHour: () -> Unit,
    onToggleTempUnit: () -> Unit,
    onToggleWindUnit: () -> Unit
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
                Icon(Icons.Outlined.ChevronLeft, contentDescription = stringResource(R.string.previous_hour), tint = contentColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            val iconName = spotForecast.weatherSymbolName(hour)
            // Mapping simple icon names to Material Symbols (Outlined)
            val icon = when (iconName) {
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
                modifier = Modifier.size(64.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            val rawTemp = forecast?.temperatureReal(hour) ?: forecast?.temperature(hour)
            val displayTemp = rawTemp?.let { Temperature(it).value(tempUnit).toInt() }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${displayTemp ?: "--"}${tempUnit.label}",
                    style = MaterialTheme.typography.displayMedium,
                    color = contentColor,
                    modifier = Modifier.clickable { onToggleTempUnit() }
                )
                
                spotForecast.forecasts.precipitationAgreement(hour)?.let { agreement ->
                    if (agreement.percentage > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${agreement.percentage}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Cyan
                            )
                            Icon(
                                imageVector = Icons.Outlined.WaterDrop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.Cyan
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(onClick = onNextHour) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = stringResource(R.string.next_hour), tint = contentColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wind info
        val windSpeed = forecast?.windSpeed(hour)
        if (windSpeed != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Air, contentDescription = null, tint = contentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${Knots(windSpeed).value(windUnit).toInt()} ${windUnit.label}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor,
                    modifier = Modifier.clickable { onToggleWindUnit() }
                )
            }
            Text(
                text = forecast.windDirectionName(hour) ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Moon phase and illumination
        ForecastMoonCalendar.info(spotForecast, spotForecast.forecastDate(hour) ?: Date())?.let { moon ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val phaseIcon = when(moon.symbolName) {
                    "moonphase.new.moon" -> R.drawable.ic_moon_new
                    "moonphase.waxing.crescent" -> R.drawable.ic_moon_waxing_crescent
                    "moonphase.first.quarter" -> R.drawable.ic_moon_first_quarter
                    "moonphase.waxing.gibbous" -> R.drawable.ic_moon_waxing_gibbous
                    "moonphase.full.moon" -> R.drawable.ic_moon_full
                    "moonphase.waning.gibbous" -> R.drawable.ic_moon_waning_gibbous
                    "moonphase.last.quarter" -> R.drawable.ic_moon_last_quarter
                    "moonphase.waning.crescent" -> R.drawable.ic_moon_waning_crescent
                    else -> R.drawable.ic_moon_new
                }
                Icon(
                    painter = painterResource(id = phaseIcon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = "${stringResource(moon.phaseNameResId)} · ${moon.illuminationText}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
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
                text = stringResource(R.string.forecast_insight),
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
fun WindDetailsSection(
    spotForecast: SpotForecast, 
    selectedHour: String?, 
    windUnit: WindSpeedUnit, 
    tempUnit: TemperatureUnit,
    onToggleWindUnit: () -> Unit,
    onToggleTempUnit: () -> Unit
) {
    val forecast = spotForecast.forecast ?: return
    val hour = selectedHour ?: spotForecast.currentForecastHour ?: return
    
    DetailCard(title = stringResource(R.string.wind_section)) {
        DetailRow(
            icon = Icons.Outlined.Air,
            label = stringResource(R.string.wind_speed),
            value = "${Knots(forecast.windSpeed(hour) ?: 0.0).value(windUnit).toInt()} ${windUnit.label}",
            onClick = onToggleWindUnit
        )
        DetailRow(
            icon = Icons.Outlined.WindPower,
            label = stringResource(R.string.wind_gusts),
            value = "${Knots(forecast.windGustsKnots(hour) ?: 0.0).value(windUnit).toInt()} ${windUnit.label}",
            onClick = onToggleWindUnit
        )
        DetailRow(
            icon = Icons.Outlined.Explore,
            label = stringResource(R.string.wind_direction),
            value = forecast.windDirectionName(hour) ?: "--"
        )
        forecast.windChill(hour)?.let { chill ->
            DetailRow(
                icon = Icons.Outlined.AcUnit,
                label = stringResource(R.string.wind_chill),
                value = "${Temperature(chill).value(tempUnit).toInt()}${tempUnit.label}",
                onClick = onToggleTempUnit
            )
        }
    }
}

@Composable
fun WeatherDetailsSection(
    spotForecast: SpotForecast, 
    selectedHour: String?,
    precipUnit: PrecipitationUnit,
    pressureUnit: PressureUnit,
    freezingLevelUnit: FreezingLevelUnit,
    waveUnit: WaveHeightUnit,
    onTogglePrecipUnit: () -> Unit,
    onTogglePressureUnit: () -> Unit,
    onToggleFreezingLevelUnit: () -> Unit,
    onToggleWaveUnit: () -> Unit
) {
    val forecast = spotForecast.forecast ?: return
    val hour = selectedHour ?: spotForecast.currentForecastHour ?: return
    val hasWaveData = forecast.waveHeight(hour) != null
    
    DetailCard(title = stringResource(R.string.weather_section)) {
        DetailRow(
            icon = Icons.Outlined.Cloud,
            label = stringResource(R.string.cloud_cover),
            value = "${forecast.cloudCoverTotal(hour) ?: 0}%"
        )
        DetailRow(
            icon = Icons.Outlined.WaterDrop,
            label = stringResource(R.string.humidity),
            value = "${forecast.relativeHumidity(hour) ?: 0}%"
        )
        DetailRow(
            icon = Icons.Outlined.Compress,
            label = stringResource(R.string.pressure),
            value = "${AtmosphericPressure(forecast.seaLevelPressure(hour) ?: 0.0).value(pressureUnit).toInt()} ${pressureUnit.label}",
            onClick = onTogglePressureUnit
        )
        val precip = forecast.precipitation(hour) ?: forecast.precipitation1(hour) ?: 0.0
        val precipVal = Precipitation(precip).value(precipUnit)
        val precipText = if (precipVal < 1 && precipVal > 0) String.format(Locale.getDefault(), "%.1f", precipVal) else precipVal.toInt().toString()
        
        DetailRow(
            icon = Icons.Outlined.BeachAccess,
            label = stringResource(R.string.precipitation),
            value = "$precipText ${precipUnit.label}",
            onClick = onTogglePrecipUnit
        )
        DetailRow(
            icon = Icons.Outlined.Landscape,
            label = stringResource(R.string.freezing_level),
            value = "${FreezingLevel(forecast.freezingLevelHeightInMeters(hour) ?: 0.0).value(freezingLevelUnit).toInt()} ${freezingLevelUnit.label}",
            onClick = onToggleFreezingLevelUnit
        )
        
        if (hasWaveData) {
            val waveH = forecast.waveHeight(hour) ?: 0.0
            DetailRow(
                icon = Icons.Outlined.Water,
                label = stringResource(R.string.wave_height),
                value = "${String.format(Locale.getDefault(), "%.1f", WaveHeight(waveH).value(waveUnit))} ${waveUnit.label}",
                onClick = onToggleWaveUnit
            )
            DetailRow(
                icon = Icons.Outlined.Timer,
                label = stringResource(R.string.wave_period),
                value = "${forecast.wavePeriod(hour)?.let { String.format(Locale.getDefault(), "%.0f", it) } ?: "--"} s"
            )
            DetailRow(
                icon = Icons.Outlined.Explore,
                label = stringResource(R.string.wave_direction),
                value = forecast.waveDirection(hour)?.let { 
                    val travel = (it + 180.0) % 360.0
                    val arrows = listOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")
                    arrows[( ((travel + 22.5) / 45.0).toInt() % 8 )]
                } ?: "--"
            )
        }
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
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Bold,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
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
