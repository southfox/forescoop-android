//
//  ForecastWidgetEntry.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.data.model

data class ForecastWidgetEntry(
    val locationName: String,
    val temperature: String,
    val temperatureCelsius: Double,
    val wind: String,
    val gust: String,
    val windDirection: String,
    val windDirectionDegrees: Double,
    val rain: String,
    val humidity: String,
    val cloudCover: String,
    val pressure: String,
    val symbolName: String,
    val condition: String,
    val high: String,
    val low: String,
    val insight: String,
    val hourly: List<ForecastWidgetHour> = emptyList(),
    val daily: List<ForecastWidgetDay> = emptyList(),
    val errorMessage: String? = null
) {
    companion object {
        val placeholder = ForecastWidgetEntry(
            locationName = "Bariloche",
            temperature = "12°C",
            temperatureCelsius = 12.0,
            wind = "10 kt",
            gust = "16 kt",
            windDirection = "W",
            windDirectionDegrees = 270.0,
            rain = "0 mm",
            humidity = "58%",
            cloudCover = "35%",
            pressure = "1015 hPa",
            symbolName = "wb_cloudy",
            condition = "Partly cloudy",
            high = "14°",
            low = "6°",
            insight = "Bariloche · Today: 12°C with 10 kt wind."
        )
    }
}

data class ForecastWidgetHour(
    val label: String,
    val symbolName: String,
    val temperature: String
)

data class ForecastWidgetDay(
    val label: String,
    val symbolName: String,
    val low: String,
    val high: String,
    val lowValue: Double,
    val highValue: Double
)
