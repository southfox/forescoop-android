//
//  ForecastHourSelector.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-23.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import org.forescoop.ventus.R
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.forecast.GridHour
import java.util.Date

@Composable
fun ForecastHourSelector(
    forecast: SpotForecast,
    selectedHour: String?,
    onHourSelected: (String) -> Unit
) {
    val hours = remember(forecast) {
        val baseHours = forecast.availableForecastHours
        forecast.insertingDaylightEvents<GridHour>(
            items = baseHours.map { GridHour.Forecast(it) },
            dateProvider = { 
                when(it) {
                    is GridHour.Forecast -> forecast.forecastDate(it.hour) ?: Date(0)
                    is GridHour.SunEvent -> it.date
                }
            },
            makeEvent = { prev, next, date, symbol ->
                GridHour.SunEvent(symbol, date, (prev as GridHour.Forecast).hour, (next as GridHour.Forecast).hour)
            }
        )
    }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Scroll to the selected hour when it changes
    LaunchedEffect(selectedHour) {
        val index = hours.indexOf(hours.find { 
            it is GridHour.Forecast && it.hour == selectedHour 
        })
        if (index != -1) {
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(hours) { gridHour ->
            val hourValue = when(gridHour) {
                is GridHour.Forecast -> gridHour.hour
                is GridHour.SunEvent -> gridHour.previousHour // Selecting a sun event picks the hour before it
            }
            val isSelected = when(gridHour) {
                is GridHour.Forecast -> gridHour.hour == selectedHour
                is GridHour.SunEvent -> false // Sun events themselves are not "selected" in the state usually
            }
            val date = when(gridHour) {
                is GridHour.Forecast -> forecast.forecastDate(gridHour.hour)
                is GridHour.SunEvent -> gridHour.date
            }
            val showDay = ForecastDateFormatter.shouldShowDay(date, forecast.tz)
            
            Column(
                modifier = Modifier
                    .clickable { onHourSelected(hourValue) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showDay) ForecastDateFormatter.formatDay(date, forecast.tz) else " ",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = ForecastDateFormatter.formatHour(context, date, forecast.tz),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val iconName = when(gridHour) {
                    is GridHour.Forecast -> forecast.weatherSymbolName(gridHour.hour)
                    is GridHour.SunEvent -> gridHour.symbolName
                }
                
                if (iconName == "sunrise" || iconName == "sunset") {
                    Icon(
                        painter = painterResource(id = if (iconName == "sunrise") R.drawable.ic_sunrise else R.drawable.ic_sunset),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
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
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
