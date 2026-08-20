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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forescoop.ventus.data.model.Forecast
import org.forescoop.ventus.data.model.SpotForecast

@Composable
fun ForecastGridScreen(
    spotForecast: SpotForecast
) {
    val forecast = spotForecast.forecast ?: return
    val hours = spotForecast.availableForecastHours

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Forecast Grid - ${spotForecast.displayName}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // Row Labels
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                GridLabel("Hour")
                GridLabel("Wind (kt)")
                GridLabel("Gust (kt)")
                GridLabel("Dir")
                GridLabel("Temp (°C)")
                GridLabel("Cloud (%)")
                GridLabel("Precip (mm)")
            }

            // Forecast Data
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Row {
                    hours.forEach { hour ->
                        ForecastColumn(hour, forecast)
                    }
                }
            }
        }
    }
}

@Composable
fun GridLabel(text: String) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForecastColumn(hour: String, forecast: Forecast) {
    Column(modifier = Modifier.width(60.dp)) {
        DataCell(text = hour, isHeader = true)
        DataCell(text = forecast.windSpeed(hour)?.toInt()?.toString() ?: "--")
        DataCell(text = forecast.windGustsKnots(hour)?.toInt()?.toString() ?: "--")
        DataCell(text = forecast.windDirectionName(hour) ?: "--")
        DataCell(text = forecast.temperatureReal(hour)?.toInt()?.toString() ?: "--")
        DataCell(text = forecast.cloudCoverTotal(hour)?.toString() ?: "--")
        DataCell(text = forecast.precipitation(hour)?.toString() ?: "--")
    }
}

@Composable
fun DataCell(text: String, isHeader: Boolean = false) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(if (isHeader) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (isHeader) 14.sp else 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
    }
}
