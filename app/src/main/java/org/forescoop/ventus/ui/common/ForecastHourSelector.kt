package org.forescoop.ventus.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.forescoop.ventus.R
import org.forescoop.ventus.data.model.SpotForecast

@Composable
fun ForecastHourSelector(
    forecast: SpotForecast,
    selectedHour: String?,
    onHourSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val allHours = forecast.availableForecastHours
    val current = forecast.currentForecastHour
    val currentIndex = allHours.indexOf(current).coerceAtLeast(0)
    val hours = allHours.drop(currentIndex)
    val selection = selectedHour?.takeIf { it in hours } ?: current?.takeIf { it in hours } ?: hours.firstOrNull()
    val index = hours.indexOf(selection)
    var expanded by remember { mutableStateOf(false) }

    fun label(hour: String): String {
        val date = forecast.forecastDate(hour)
        val day = if (ForecastDateFormatter.isToday(date, forecast.tz)) context.getString(R.string.today)
                  else ForecastDateFormatter.formatDay(date, forecast.tz)
        return "$day, ${ForecastDateFormatter.formatHour(context, date, forecast.tz)}"
    }

    LaunchedEffect(selection) {
        if (selection != null && selectedHour != selection) onHourSelected(selection)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = { onHourSelected(hours[index - 1]) }, enabled = index > 0) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = stringResource(R.string.previous_hour))
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            TextButton(onClick = { expanded = true }, enabled = selection != null) {
                Text(selection?.let(::label) ?: "--", fontWeight = if (selection == current) FontWeight.Bold else FontWeight.Normal)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                hours.forEach { hour ->
                    DropdownMenuItem(text = { Text(label(hour)) }, onClick = {
                        expanded = false
                        onHourSelected(hour)
                    })
                }
            }
        }
        IconButton(onClick = { onHourSelected(hours[index + 1]) }, enabled = index >= 0 && index < hours.lastIndex) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = stringResource(R.string.next_hour))
        }
    }
}
