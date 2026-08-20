//
//  ForecastWidget.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import androidx.glance.action.actionStartActivity
import org.forescoop.ventus.MainActivity
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.data.model.ForecastWidgetEntry
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.ui.common.ForecastUnitPreferences
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit

class ForecastWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = WindguruRepository()
        val prefsStore = PreferenceStore(context)
        
        val lastSpotId = prefsStore.getString("last_spot_id").first() ?: "64141"
        val windUnitStr = prefsStore.getString(PreferenceStore.KEY_WIND_UNIT).first()
        val windUnit = WindSpeedUnit.fromWindguruPreference(windUnitStr) ?: WindSpeedUnit.KNOTS
        val tempUnitStr = prefsStore.getString(PreferenceStore.KEY_TEMP_UNIT).first()
        val tempUnit = TemperatureUnit.fromWindguruPreference(tempUnitStr) ?: TemperatureUnit.CELSIUS
            
        val entry = try {
            val forecast = repo.getForecast(lastSpotId)
            ForecastWidgetEntryBuilder.build(
                context, 
                forecast, 
                ForecastUnitPreferences(temperatureUnit = tempUnit, windSpeedUnit = windUnit)
            )
        } catch (e: Exception) {
            ForecastWidgetEntry.placeholder.copy(
                errorMessage = e.message ?: "Error loading forecast"
            )
        }
        
        provideContent {
            GlanceTheme {
                ForecastWidgetContent(entry)
            }
        }
    }
}

@Composable
fun ForecastWidgetContent(entry: ForecastWidgetEntry) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(org.forescoop.ventus.R.drawable.widget_bg))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Text(
            text = entry.locationName,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ColorProvider(org.forescoop.ventus.R.color.widget_text)
            )
        )
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = entry.temperature,
                style = TextStyle(
                    fontSize = 32.sp,
                    color = ColorProvider(org.forescoop.ventus.R.color.widget_text)
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(org.forescoop.ventus.R.drawable.ic_weather_generic),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp)
            )
        }
        Text(
            text = "${entry.wind} · ${entry.windDirection}",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary)
            )
        )
    }
}

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ForecastWidget()
}
