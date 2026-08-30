//
//  ForecastDaylightSummary.kt
//  Ventus
//

package org.forescoop.ventus.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forescoop.ventus.R
import org.forescoop.ventus.data.model.ForecastMoonCalendar
import org.forescoop.ventus.data.model.SpotForecast
import java.util.Date

@Composable
fun ForecastDaylightSummary(forecast: SpotForecast, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sunrise = forecast.sunrise
    val sunset = forecast.sunset

    if (sunrise != null && sunset != null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DaylightLabel(text = sunrise, iconId = R.drawable.ic_sunrise)
                DaylightLabel(text = sunset, iconId = R.drawable.ic_sunset)
            }

            ForecastMoonCalendar.info(forecast, Date())?.let { moon ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${stringResource(moon.phaseNameResId)} · ${moon.illuminationText}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))

                    moon.moonrise?.let { 
                        DaylightLabel(
                            text = ForecastDateFormatter.formatHour(context, it, forecast.tz),
                            iconId = R.drawable.ic_moonrise,
                            isCompact = true
                        )
                    }
                    moon.moonset?.let { 
                        DaylightLabel(
                            text = ForecastDateFormatter.formatHour(context, it, forecast.tz),
                            iconId = R.drawable.ic_moonset,
                            isCompact = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaylightLabel(text: String, iconId: Int, isCompact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp)) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(if (isCompact) 14.dp else 20.dp),
            tint = if (isCompact) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
