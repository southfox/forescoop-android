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
import org.forescoop.ventus.data.model.SpotForecast

@Composable
fun ForecastHourSelector(
    forecast: SpotForecast,
    selectedHour: String?,
    onHourSelected: (String) -> Unit
) {
    val hours = forecast.availableForecastHours
    val currentHour = forecast.currentForecastHour
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Scroll to the selected hour when it changes
    LaunchedEffect(selectedHour) {
        val index = hours.indexOf(selectedHour)
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
        items(hours) { hour ->
            val isSelected = hour == selectedHour
            val date = forecast.forecastDate(hour)
            val showDay = ForecastDateFormatter.shouldShowDay(hour, currentHour)
            
            Column(
                modifier = Modifier
                    .clickable { onHourSelected(hour) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showDay) ForecastDateFormatter.formatDay(date) else " ",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = ForecastDateFormatter.formatHour(context, date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val iconName = forecast.weatherSymbolName(hour)
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
