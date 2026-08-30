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
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import androidx.glance.action.actionStartActivity
import androidx.compose.ui.unit.DpSize
import org.forescoop.ventus.MainActivity
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.data.model.ForecastWidgetEntry
import org.forescoop.ventus.data.model.ForecastWidgetHour
import org.forescoop.ventus.data.model.ForecastWidgetDay
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.ui.common.ForecastUnitPreferences
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit

class ForecastWidget : GlanceAppWidget() {
    
    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(200.dp, 100.dp)
        private val BIG_SQUARE = DpSize(200.dp, 200.dp)
        private val EXTRA_LARGE = DpSize(300.dp, 200.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, BIG_SQUARE, EXTRA_LARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as VentusApplication
        val repo = WindguruRepository(app.windguruService)
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
    val size = LocalSize.current
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(org.forescoop.ventus.R.drawable.widget_bg))
            .clickable(actionStartActivity<MainActivity>())
    ) {
        when {
            size.width >= 300.dp -> BigWidget(entry)
            size.width >= 200.dp && size.height >= 200.dp -> LargeWidget(entry)
            size.width >= 200.dp -> MediumWidget(entry)
            else -> SmallWidget(entry)
        }
    }
}

@Composable
private fun SmallWidget(entry: ForecastWidgetEntry) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = entry.locationName,
            maxLines = 1,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text))
        )
        Image(
            provider = ImageProvider(getWeatherIconRes(entry.symbolName)),
            contentDescription = null,
            modifier = GlanceModifier.size(36.dp)
        )
        Text(
            text = entry.temperature,
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ColorProvider(org.forescoop.ventus.R.color.widget_text))
        )
        Text(
            text = "${entry.high} ${entry.low}",
            style = TextStyle(fontSize = 10.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary))
        )
    }
}

@Composable
private fun MediumWidget(entry: ForecastWidgetEntry) {
    Row(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = entry.locationName,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.temperature,
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorProvider(org.forescoop.ventus.R.color.widget_text))
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(getWeatherIconRes(entry.symbolName)),
                    contentDescription = null,
                    modifier = GlanceModifier.size(24.dp)
                )
            }
            Text(
                text = "${entry.high} / ${entry.low} · ${entry.wind}",
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary))
            )
        }
        
        Row(modifier = GlanceModifier.wrapContentWidth()) {
            entry.hourly.take(4).forEach { hour ->
                HourlyItem(hour)
            }
        }
    }
}

@Composable
private fun LargeWidget(entry: ForecastWidgetEntry) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
        MediumWidgetHeader(entry)
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            entry.hourly.take(5).forEach { hour ->
                HourlyItem(hour)
            }
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(ColorProvider(Color(0x22FFFFFF)))) {}
        Spacer(modifier = GlanceModifier.height(4.dp))
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            entry.daily.take(4).forEach { day ->
                DailyRow(day)
            }
        }
    }
}

@Composable
private fun BigWidget(entry: ForecastWidgetEntry) {
    Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                MediumWidgetHeader(entry)
                Spacer(modifier = GlanceModifier.height(8.dp))
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    entry.hourly.take(5).forEach { hour ->
                        HourlyItem(hour)
                    }
                }
            }
            Spacer(modifier = GlanceModifier.width(16.dp))
            Column(modifier = GlanceModifier.width(100.dp)) {
                MetricRow(label = "Humedad", value = entry.humidity)
                MetricRow(label = "Nubosidad", value = entry.cloudCover)
                MetricRow(label = "Presión", value = entry.pressure)
                MetricRow(label = "Ráfagas", value = entry.gust)
            }
        }
        
        Spacer(modifier = GlanceModifier.height(12.dp))
        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(ColorProvider(Color(0x22FFFFFF)))) {}
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                entry.daily.take(5).forEach { day ->
                    DailyRow(day)
                }
            }
            Spacer(modifier = GlanceModifier.width(16.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Resumen del Pronóstico",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(org.forescoop.ventus.R.color.widget_text))
                )
                Text(
                    text = entry.insight,
                    style = TextStyle(fontSize = 10.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary))
                )
            }
        }
    }
}

@Composable
private fun MediumWidgetHeader(entry: ForecastWidgetEntry) {
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = entry.locationName,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text))
            )
            Text(
                text = entry.condition,
                style = TextStyle(fontSize = 11.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary))
            )
        }
        Column(horizontalAlignment = Alignment.Horizontal.End) {
            Text(
                text = entry.temperature,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ColorProvider(org.forescoop.ventus.R.color.widget_text))
            )
            Text(
                text = "${entry.high} / ${entry.low}",
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary))
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(text = label, style = TextStyle(fontSize = 9.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary)))
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(text = value, style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorProvider(org.forescoop.ventus.R.color.widget_text)))
    }
}

@Composable
private fun HourlyItem(hour: ForecastWidgetHour) {
    Column(
        modifier = GlanceModifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(text = hour.label, style = TextStyle(fontSize = 10.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text_secondary)))
        Image(
            provider = ImageProvider(getWeatherIconRes(hour.symbolName)),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp)
        )
        Text(text = hour.temperature, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ColorProvider(org.forescoop.ventus.R.color.widget_text)))
    }
}

@Composable
private fun DailyRow(day: ForecastWidgetDay) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(text = day.label, modifier = GlanceModifier.width(40.dp), style = TextStyle(fontSize = 12.sp, color = ColorProvider(org.forescoop.ventus.R.color.widget_text)))
        Image(
            provider = ImageProvider(getWeatherIconRes(day.symbolName)),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp)
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(text = day.high, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorProvider(org.forescoop.ventus.R.color.widget_text)))
    }
}

private fun getWeatherIconRes(symbolName: String): Int {
    return when (symbolName) {
        "wb_sunny" -> org.forescoop.ventus.R.drawable.ic_sunrise
        "wb_cloudy" -> org.forescoop.ventus.R.drawable.ic_weather_generic
        "nightlight" -> org.forescoop.ventus.R.drawable.ic_sunset
        "cloud" -> org.forescoop.ventus.R.drawable.ic_weather_generic
        "ac_unit" -> org.forescoop.ventus.R.drawable.ic_weather_generic
        "umbrella" -> org.forescoop.ventus.R.drawable.ic_weather_generic
        "water_drop" -> org.forescoop.ventus.R.drawable.ic_weather_generic
        "moonphase.new.moon" -> org.forescoop.ventus.R.drawable.ic_moon_new
        "moonphase.waxing.crescent" -> org.forescoop.ventus.R.drawable.ic_moon_waxing_crescent
        "moonphase.first.quarter" -> org.forescoop.ventus.R.drawable.ic_moon_first_quarter
        "moonphase.waxing.gibbous" -> org.forescoop.ventus.R.drawable.ic_moon_waxing_gibbous
        "moonphase.full.moon" -> org.forescoop.ventus.R.drawable.ic_moon_full
        "moonphase.waning.gibbous" -> org.forescoop.ventus.R.drawable.ic_moon_waning_gibbous
        "moonphase.last.quarter" -> org.forescoop.ventus.R.drawable.ic_moon_last_quarter
        "moonphase.waning.crescent" -> org.forescoop.ventus.R.drawable.ic_moon_waning_crescent
        else -> org.forescoop.ventus.R.drawable.ic_weather_generic
    }
}

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ForecastWidget()
}
