//
//  SettingsScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.R
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.ui.common.LambdaViewModelFactory
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit

@Composable
private fun rememberSettingsViewModel(): SettingsViewModel {
    val application = LocalContext.current.applicationContext as VentusApplication
    return viewModel(factory = LambdaViewModelFactory { SettingsViewModel(application, application.analytics) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = rememberSettingsViewModel(),
    onNavigateBack: () -> Unit
) {
    val windUnit by viewModel.windUnit.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val gridHourStep by viewModel.gridHourStep.collectAsState()
    val weatherBackgroundType by viewModel.weatherBackgroundType.collectAsState()
    val appearanceMode by viewModel.appearanceMode.collectAsState()
    val themeType by viewModel.themeType.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()
    val severeWeatherAlerts by viewModel.severeWeatherAlerts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(stringResource(R.string.done), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Forecast Hours Section
            item {
                SettingsSection(
                    title = stringResource(R.string.forecast_hours),
                    footer = stringResource(R.string.forecast_hours_footer)
                ) {
                    Text(
                        stringResource(R.string.every_x_hours, gridHourStep),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Slider(
                        value = gridHourStep.toFloat(),
                        onValueChange = { viewModel.setGridHourStep(it.toInt()) },
                        valueRange = 1f..6f,
                        steps = 4,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("6", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Animations Section
            item {
                SettingsSection(title = stringResource(R.string.animations)) {
                    Text(
                        stringResource(R.string.weather_background),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    val backgrounds = listOf(
                        "None" to R.string.none,
                        "Native" to R.string.native_anim,
                        "Metal" to R.string.metal,
                        "SpriteKit" to R.string.spritekit,
                        "Lottie - Adriana" to null,
                        "Lottie - Asad" to null
                    )
                    backgrounds.forEach { (type, resId) ->
                        val icon = when(type) {
                            "None" -> Icons.Outlined.Block
                            "Native" -> Icons.Outlined.AutoAwesome
                            "Metal" -> Icons.Outlined.Memory
                            "SpriteKit" -> Icons.Outlined.Animation
                            else -> Icons.Outlined.PlayCircle
                        }
                        SettingsOptionRow(
                            title = if (resId != null) stringResource(resId) else type,
                            icon = icon,
                            isSelected = weatherBackgroundType == type,
                            onClick = { viewModel.setWeatherBackgroundType(type) }
                        )
                    }
                }
            }

            // Appearance Section
            item {
                SettingsSection(
                    title = stringResource(R.string.appearance),
                    footer = stringResource(R.string.appearance_footer)
                ) {
                    val modes = listOf(
                        "System" to R.string.system,
                        "Light" to R.string.light,
                        "Dark" to R.string.dark
                    )
                    modes.forEach { (mode, resId) ->
                        val icon = when(mode) {
                            "System" -> Icons.Outlined.Contrast
                            "Light" -> Icons.Outlined.WbSunny
                            else -> Icons.Outlined.Nightlight
                        }
                        SettingsOptionRow(
                            title = stringResource(resId),
                            icon = icon,
                            isSelected = appearanceMode == mode,
                            onClick = { viewModel.setAppearanceMode(mode) }
                        )
                    }
                }
            }

            // Theme Section
            item {
                SettingsSection(
                    title = stringResource(R.string.theme),
                    footer = stringResource(R.string.theme_footer)
                ) {
                    val themes = listOf(
                        "System" to R.string.system,
                        "Liquid Glass" to R.string.liquid_glass,
                        "Ocean" to R.string.ocean,
                        "Forest" to R.string.forest,
                        "Sunset" to R.string.sunset,
                        "Arctic" to R.string.arctic,
                        "Lavender" to R.string.lavender,
                        "Desert" to R.string.desert
                    )
                    themes.forEach { (theme, resId) ->
                        SettingsOptionRow(
                            title = stringResource(resId),
                            icon = Icons.Outlined.Palette,
                            isSelected = themeType == theme,
                            onClick = { viewModel.setThemeType(theme) }
                        )
                    }
                }
            }

            // Language Section
            item {
                SettingsSection(
                    title = stringResource(R.string.language),
                    footer = stringResource(R.string.language_footer)
                ) {
                    val languages = listOf(
                        "System" to "System",
                        "English (US)" to "en-US",
                        "Español (España)" to "es-ES",
                        "Deutsch" to "de-DE",
                        "Français" to "fr-FR",
                        "Português (Brasil)" to "pt-BR"
                    )
                    languages.forEach { (label, code) ->
                        SettingsOptionRow(
                            title = label,
                            icon = Icons.Outlined.Language,
                            isSelected = languageCode == code,
                            onClick = { viewModel.setLanguageCode(code) }
                        )
                    }
                }
            }

            // Notifications Section
            item {
                SettingsSection(
                    title = stringResource(R.string.notifications),
                    footer = stringResource(R.string.notifications_footer)
                ) {
                    SettingsToggleRow(
                        title = stringResource(R.string.severe_weather_alerts),
                        checked = severeWeatherAlerts,
                        onCheckedChange = { viewModel.setSevereWeatherAlerts(it) }
                    )
                    SettingsNavItemRow(
                        title = stringResource(R.string.forecast_alerts),
                        icon = Icons.Outlined.Notifications,
                        onClick = { /* Navigate to alerts */ }
                    )
                }
            }
            
            // Units Section
            item {
                SettingsSection(title = stringResource(R.string.units)) {
                    Text(
                        stringResource(R.string.wind_speed),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    WindSpeedUnit.entries.forEach { unit ->
                        SettingsOptionRow(
                            title = unit.label,
                            icon = Icons.Outlined.Air,
                            isSelected = windUnit == unit,
                            onClick = { viewModel.setWindUnit(unit) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.temperature),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    TemperatureUnit.entries.forEach { unit ->
                        SettingsOptionRow(
                            title = unit.label,
                            icon = Icons.Outlined.DeviceThermostat,
                            isSelected = tempUnit == unit,
                            onClick = { viewModel.setTempUnit(unit) }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 4.dp),
            content = content
        )
        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsOptionRow(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp).weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsNavItemRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp).weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
