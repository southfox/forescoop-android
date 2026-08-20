//
//  SpotForecast.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@JsonAdapter(SpotForecastDeserializer::class)
class SpotForecast(
    idSpot: String? = null,
    spotName: String? = null,
    country: String? = null,
    nickname: String? = null,
    idCountry: Int = 0,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    altitude: Int = 0,
    gmtHourOffset: Int = 0,
    sunrise: String? = null,
    sunset: String? = null,
    models: List<Int> = emptyList(),
    tides: String? = null,
    tz: String? = null,
    var currentModel: String? = null,
    val forecasts: List<ForecastModel> = emptyList()
) : SpotInfo(
    idSpot, spotName, country, nickname, idCountry, latitude, longitude,
    altitude, gmtHourOffset, sunrise, sunset, models, tides, tz
) {
    val availableForecastHours: List<String>
        get() = forecast?.availableHours ?: emptyList()

    val forecastModel: ForecastModel?
        get() = forecasts.find { it.model == currentModel } ?: forecasts.firstOrNull()

    val forecast: Forecast?
        get() = forecastModel?.info

    fun isDaylight(date: Date): Boolean {
        val sunriseDate = daylightDate(sunrise, date)
        val sunsetDate = daylightDate(sunset, date)
        if (sunriseDate == null || sunsetDate == null) return true
        return date >= sunriseDate && date <= sunsetDate
    }

    private fun daylightDate(time: String?, date: Date): Date? {
        if (time == null) return null
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return null
        
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(tz ?: "GMT${if (gmtHourOffset >= 0) "+" else ""}$gmtHourOffset"))
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, parts[0])
        calendar.set(Calendar.MINUTE, parts[1])
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun weatherSymbolName(hour: String?): String {
        val precipitation = forecast?.precipitation(hour) ?: forecast?.precipitation1(hour) ?: 0.0
        val temp = forecast?.temperatureReal(hour) ?: forecast?.temperature(hour) ?: 0.0
        val cloudCover = forecast?.cloudCoverTotal(hour) ?: 0
        val isNight = !isDaylight(forecastDate(hour) ?: Date())

        return when {
            precipitation > 0.0 && temp <= 0 -> "ac_unit" // Snow
            precipitation > 8.0 -> "umbrella" // Heavy rain
            precipitation > 0.0 -> "cloudy_filled" // Rain
            cloudCover <= 20 -> if (isNight) "nightlight" else "wb_sunny"
            cloudCover <= 50 -> if (isNight) "cloudy_night" else "wb_cloudy"
            else -> "cloud"
        }
    }

    fun forecastDate(hour: String?, referenceDate: Date = Date()): Date? {
        val offset = hour?.toIntOrNull() ?: return null
        val initStamp = forecast?.initStamp ?: 0
        if (initStamp > 0) {
            return Date(initStamp * 1000 + offset * 3600L * 1000L)
        }
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(tz ?: "GMT${if (gmtHourOffset >= 0) "+" else ""}$gmtHourOffset"))
        calendar.time = referenceDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.HOUR_OF_DAY, offset)
        return calendar.time
    }

    override fun toString(): String {
        return super.toString() + "\nSpotForecast: \n$currentModel\n" +
                forecasts.joinToString("\n") { it.toString() }
    }
}

class SpotForecastDeserializer : JsonDeserializer<SpotForecast> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SpotForecast {
        val jsonObject = json.asJsonObject
        
        // Deserialize base SpotInfo fields manually or via context if possible.
        // For simplicity, let's pull them directly.
        val idSpot = jsonObject.get("id_spot")?.asString
        val spotName = jsonObject.get("spotname")?.asString
        val country = jsonObject.get("country")?.asString
        val nickname = jsonObject.get("nickname")?.asString
        val idCountry = jsonObject.get("id_country")?.asInt ?: 0
        val latitude = jsonObject.get("lat")?.asDouble ?: 0.0
        val longitude = jsonObject.get("lon")?.asDouble ?: 0.0
        val altitude = jsonObject.get("alt")?.asInt ?: 0
        val gmtHourOffset = jsonObject.get("gmt_hour_offset")?.asInt ?: 0
        val sunrise = jsonObject.get("sunrise")?.asString
        val sunset = jsonObject.get("sunset")?.asString
        val models = mutableListOf<Int>()
        jsonObject.get("models")?.asJsonArray?.forEach { models.add(it.asInt) }
        val tides = jsonObject.get("tides")?.asString
        val tz = jsonObject.get("tz")?.asString

        val forecasts = mutableListOf<ForecastModel>()
        jsonObject.get("forecast")?.asJsonObject?.entrySet()?.forEach { entry ->
            val modelId = entry.key
            val info = entry.value.asJsonObject
            if (info.has(TypeOfWeather.WINDSPD.name)) {
                val forecast = context.deserialize<Forecast>(info, Forecast::class.java)
                forecast.gmtHourOffset = gmtHourOffset
                forecasts.add(ForecastModel(modelId, forecast))
            }
        }
        
        val defaultModel = "3" // Windguru default
        val currentModel = forecasts.find { it.model == defaultModel }?.model ?: forecasts.firstOrNull()?.model

        return SpotForecast(
            idSpot, spotName, country, nickname, idCountry, latitude, longitude,
            altitude, gmtHourOffset, sunrise, sunset, models, tides, tz,
            currentModel, forecasts
        )
    }
}
