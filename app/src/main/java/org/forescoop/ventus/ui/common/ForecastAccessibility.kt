package org.forescoop.ventus.ui.common

import android.content.Context
import org.forescoop.ventus.R
import org.forescoop.ventus.data.model.SpotForecast
import java.util.Locale
import kotlin.math.roundToInt

object ForecastAccessibility {
    fun summary(context: Context, forecast: SpotForecast, hour: String?): String {
        val selectedHour = hour ?: forecast.currentForecastHour
        val weather = forecast.forecast
        val temperature = weather?.temperatureReal(selectedHour) ?: weather?.temperature(selectedHour)
        val wind = weather?.windSpeed(selectedHour)
        val rain = weather?.precipitation1(selectedHour) ?: weather?.precipitation(selectedHour)

        val values = mutableListOf<String>()
        values.add(forecast.displayName)
        
        temperature?.let {
            values.add(context.getString(R.string.accessibility_temp_label, it.roundToInt()))
        }
        
        wind?.let {
            values.add(context.getString(R.string.accessibility_wind_label, it.roundToInt()))
        }
        
        rain?.let {
            values.add(context.getString(R.string.accessibility_rain_label, it))
        }
        
        return values.joinToString(", ")
    }
}
