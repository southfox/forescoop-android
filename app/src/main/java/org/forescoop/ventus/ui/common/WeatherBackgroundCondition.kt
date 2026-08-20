//
//  WeatherBackgroundCondition.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import org.forescoop.ventus.data.model.SpotForecast
import java.util.Date

data class WeatherBackgroundCondition(
    val symbolNames: List<String>,
    val precipitationMillimeters: Double,
    val windSpeedKnots: Double,
    val windGustKnots: Double,
    val windDirectionDegrees: Double,
    val cloudCoverPercent: Int,
    val humidityPercent: Int,
    val temperatureCelsius: Double,
    val forecastDate: Date
) {
    constructor(forecast: SpotForecast, hour: String? = null) : this(
        symbolNames = forecast.weatherSymbolNames(hour ?: forecast.currentForecastHour),
        precipitationMillimeters = forecast.forecast?.precipitation(hour ?: forecast.currentForecastHour)
            ?: forecast.forecast?.precipitation1(hour ?: forecast.currentForecastHour) ?: 0.0,
        windSpeedKnots = forecast.forecast?.windSpeed(hour ?: forecast.currentForecastHour) ?: 0.0,
        windGustKnots = forecast.forecast?.windGustsKnots(hour ?: forecast.currentForecastHour) ?: 0.0,
        windDirectionDegrees = forecast.forecast?.windDirection(hour ?: forecast.currentForecastHour) ?: 270.0,
        cloudCoverPercent = forecast.forecast?.cloudCoverTotal(hour ?: forecast.currentForecastHour) ?: 0,
        humidityPercent = forecast.forecast?.relativeHumidity(hour ?: forecast.currentForecastHour) ?: 0,
        temperatureCelsius = forecast.forecast?.temperatureReal(hour ?: forecast.currentForecastHour)
            ?: forecast.forecast?.temperature(hour ?: forecast.currentForecastHour) ?: 0.0,
        forecastDate = forecast.forecastDate(hour ?: forecast.currentForecastHour) ?: Date()
    )

    val isSunny: Boolean get() = symbolNames.any { it.contains("sun") || it.contains("wb_sunny") }
    val isNight: Boolean get() = symbolNames.any { it.contains("moon") || it.contains("nightlight") }
    val isCloudy: Boolean get() = symbolNames.any { it.contains("cloud") }
    val isWindy: Boolean get() = symbolNames.any { it == "wind" || it.contains("tornado") }
    val isRainy: Boolean get() = symbolNames.any { it.contains("rain") || it.contains("drizzle") }
    val isSnowy: Boolean get() = symbolNames.any { it.contains("snow") }
    val isFoggy: Boolean get() = symbolNames.any { it.contains("fog") } || (humidityPercent >= 95 && cloudCoverPercent >= 80)
    val hasPrecipitation: Boolean get() = precipitationMillimeters > 0
    val isHeavyRain: Boolean get() = isRainy && precipitationMillimeters >= 8
    val showsRainbow: Boolean get() = isSunny && precipitationMillimeters > 0 && !isSnowy
    val isFreezing: Boolean get() = temperatureCelsius <= 0
    val isHot: Boolean get() = temperatureCelsius >= 30
    val windTravelDirectionDegrees: Double get() = windDirectionDegrees + 180
}
