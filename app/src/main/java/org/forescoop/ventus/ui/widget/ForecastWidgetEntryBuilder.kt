//
//  ForecastWidgetEntryBuilder.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.widget

import android.content.Context
import org.forescoop.ventus.data.model.ForecastWidgetDay
import org.forescoop.ventus.data.model.ForecastWidgetEntry
import org.forescoop.ventus.data.model.ForecastWidgetHour
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.common.ForecastIntelligence
import org.forescoop.ventus.ui.common.ForecastUnitPreferences
import org.forescoop.ventus.ui.common.Knots
import org.forescoop.ventus.ui.common.Temperature
import java.text.SimpleDateFormat
import java.util.Locale

object ForecastWidgetEntryBuilder {

    fun build(
        context: Context,
        forecast: SpotForecast,
        prefs: ForecastUnitPreferences
    ): ForecastWidgetEntry {
        val hour = forecast.currentForecastHour ?: "0"
        val weather = forecast.forecast
        val locale = Locale.getDefault()
        
        val tempValue = weather?.temperatureReal(hour) ?: weather?.temperature(hour) ?: 0.0
        val temperature = String.format(locale, "%.0f%s", Temperature(tempValue).value(prefs.temperatureUnit), prefs.temperatureUnit.label)
        
        val windValue = weather?.windSpeed(hour) ?: 0.0
        val wind = String.format(locale, "%.0f %s", Knots(windValue).value(prefs.windSpeedUnit), prefs.windSpeedUnit.label)
        
        val gustValue = weather?.windGustsKnots(hour) ?: 0.0
        val gust = String.format(locale, "%.0f %s", Knots(gustValue).value(prefs.windSpeedUnit), prefs.windSpeedUnit.label)
        
        val rainValue = weather?.precipitation(hour) ?: weather?.precipitation1(hour) ?: 0.0
        val rain = String.format(locale, "%.1f mm", rainValue)
        
        val cloud = weather?.cloudCoverTotal(hour) ?: 0
        val cloudCover = "$cloud%"
        
        val hum = weather?.relativeHumidity(hour) ?: 0
        val humidity = "$hum%"
        
        val press = weather?.seaLevelPressure(hour) ?: 1013.0
        val pressure = String.format(locale, "%.0f hPa", press)

        val range = forecast.dailyTemperatureRange(hour)
        val high = range?.let { String.format(locale, "%.0f°", Temperature(it.second).value(prefs.temperatureUnit)) } ?: "--"
        val low = range?.let { String.format(locale, "%.0f°", Temperature(it.first).value(prefs.temperatureUnit)) } ?: "--"

        // Hourly
        val currentOffset = hour.toIntOrNull() ?: 0
        val hourly = forecast.availableForecastHours
            .mapNotNull { it.toIntOrNull()?.let { offset -> it to offset } }
            .filter { it.second > currentOffset }
            .take(6)
            .map { (h, _) ->
                val t = weather?.temperatureReal(h) ?: weather?.temperature(h) ?: 0.0
                ForecastWidgetHour(
                    label = String.format(locale, "%02d:00", h.toInt() % 24),
                    symbolName = forecast.weatherSymbolName(h),
                    temperature = String.format(locale, "%.0f°", Temperature(t).value(prefs.temperatureUnit))
                )
            }

        // Daily (simulated from available hours since Windguru response is hourly)
        // We'll take same hour in next days
        val daily = mutableListOf<ForecastWidgetDay>()
        val baseHour = currentOffset % 24
        for (day in 1..4) {
            val targetHour = (day * 24 + baseHour).toString()
            if (forecast.availableForecastHours.contains(targetHour)) {
                val t = weather?.temperatureReal(targetHour) ?: weather?.temperature(targetHour) ?: 0.0
                val date = forecast.forecastDate(targetHour) ?: java.util.Date()
                daily.add(ForecastWidgetDay(
                    label = SimpleDateFormat("EEE", locale).format(date),
                    symbolName = forecast.weatherSymbolName(targetHour),
                    low = "--", // Simplified for now
                    high = String.format(locale, "%.0f°", Temperature(t).value(prefs.temperatureUnit)),
                    lowValue = 0.0,
                    highValue = t
                ))
            }
        }

        return ForecastWidgetEntry(
            locationName = forecast.displayName,
            temperature = temperature,
            temperatureCelsius = tempValue,
            wind = wind,
            gust = gust,
            windDirection = weather?.windDirectionName(hour) ?: "--",
            windDirectionDegrees = weather?.windDirection(hour) ?: 0.0,
            rain = rain,
            humidity = humidity,
            cloudCover = cloudCover,
            pressure = pressure,
            symbolName = forecast.weatherSymbolName(hour),
            condition = forecast.weatherSymbolName(hour), // Or a mapping to a string description
            high = high,
            low = low,
            insight = ForecastIntelligence.summary(context, forecast, hour, prefs, locale),
            hourly = hourly,
            daily = daily
        )
    }
}
