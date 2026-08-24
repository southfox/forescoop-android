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

    val currentForecastHour: String?
        get() {
            val hours = availableForecastHours.mapNotNull { hour ->
                hour.toIntOrNull()?.let { hour to it }
            }
            val initStamp = forecast?.initStamp ?: 0
            if (initStamp > 0) {
                val elapsedHours = (System.currentTimeMillis() / 1000.0 - initStamp) / 3600.0
                return hours.lastOrNull { it.second.toDouble() <= elapsedHours }?.first ?: hours.firstOrNull()?.first
            }
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            return hours.lastOrNull { it.second <= currentHour }?.first ?: hours.firstOrNull()?.first
        }

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
        return weatherSymbolNames(hour).firstOrNull() ?: "wb_sunny"
    }

    fun weatherSymbolNames(hour: String?): List<String> {
        val precipitation = forecast?.precipitation(hour) ?: forecast?.precipitation1(hour) ?: 0.0
        val modelTemperature = forecast?.temperature(hour) ?: forecast?.temperatureReal(hour) ?: 0.0
        val phaseTemperature = forecast?.temperatureReal(hour) ?: forecast?.temperature(hour) ?: 0.0
        val cloudCover = forecast?.cloudCoverTotal(hour) ?: 0
        val humidity = forecast?.relativeHumidity(hour) ?: 0
        val windSpeed = forecast?.windSpeed(hour) ?: 0.0
        val windGusts = forecast?.windGustsKnots(hour) ?: 0.0
        val isNight = !isDaylight(forecastDate(hour) ?: Date())
        val symbols = mutableListOf<String>()

        val oktas = (cloudCover.toDouble() / 100.0 * 8.0).toInt()
        val isSnow = precipitation > 0 && phaseTemperature <= 0
        val isHeavyRain = precipitation > 0 && !isSnow && precipitation >= 8
        val isLightRain = precipitation > 0 && !isSnow && !isHeavyRain

        if (isSnow) {
            symbols.add("ac_unit") // snowflake
        } else if (isHeavyRain) {
            symbols.add("umbrella") // cloud.heavyrain
        } else if (isLightRain && oktas <= 2) {
            symbols.add(if (isNight) "nightlight" else "wb_sunny") // cloud.moon.rain.fill / cloud.sun.rain.fill
        } else if (isLightRain) {
            symbols.add(if (precipitation < 2) "water_drop" else "umbrella") // cloud.drizzle / cloud.rain.fill
        } else if (humidity >= 95 && cloudCover >= 80) {
            symbols.add("foggy") // cloud.fog
        } else if (oktas <= 2) {
            symbols.add(if (isNight) "nightlight" else if (modelTemperature >= 30) "wb_sunny" else "wb_sunny")
        } else if (oktas <= 4) {
            symbols.add(if (isNight) "nightlight" else "wb_cloudy")
        } else if (oktas <= 7) {
            symbols.add("cloud")
        } else {
            symbols.add("cloud") // cloud.fill
        }
        
        return symbols
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

    fun sunriseDate(onDate: Date): Date? = daylightEventDate(sunrise, onDate)
    fun sunsetDate(onDate: Date): Date? = daylightEventDate(sunset, onDate)

    private fun daylightEventDate(time: String?, referenceDate: Date): Date? {
        if (time == null) return null
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return null
        
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(tz ?: "GMT${if (gmtHourOffset >= 0) "+" else ""}$gmtHourOffset"))
        calendar.time = referenceDate
        calendar.set(Calendar.HOUR_OF_DAY, parts[0])
        calendar.set(Calendar.MINUTE, parts[1])
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun <Item> insertingDaylightEvents(
        items: List<Item>,
        dateProvider: (Item) -> Date,
        makeEvent: (previous: Item, next: Item, eventDate: Date, symbolName: String) -> Item
    ): List<Item> {
        if (items.size < 2) return items
        val result = mutableListOf<Item>()
        val insertedSymbols = mutableSetOf<String>()
        
        var nextSunriseFound = false
        var nextSunsetFound = false
        val now = Date()

        for (index in items.indices) {
            val item = items[index]
            result.add(item)
            if (index < items.size - 1) {
                val next = items[index + 1]
                val start = dateProvider(item)
                val end = dateProvider(next)
                if (start < end) {
                    val events = mutableListOf<Pair<Date, String>>()
                    
                    // Try to find next sunrise if not already found
                    if (!nextSunriseFound) {
                        val sunrise = sunriseDate(start)
                        if (sunrise != null && sunrise > now && sunrise > start && sunrise < end) {
                            events.add(sunrise to "sunrise")
                            nextSunriseFound = true
                        }
                    }

                    // Try to find next sunset if not already found
                    if (!nextSunsetFound) {
                        val sunset = sunsetDate(start)
                        if (sunset != null && sunset > now && sunset > start && sunset < end) {
                            events.add(sunset to "sunset")
                            nextSunsetFound = true
                        }
                    }
                    
                    events.sortBy { it.first }
                    for (event in events) {
                        val key = "${event.second}:${event.first.time}"
                        if (!insertedSymbols.contains(key)) {
                            insertedSymbols.add(key)
                            result.add(makeEvent(item, next, event.first, event.second))
                        }
                    }
                }
            }
        }
        return result
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
        
        // Check for Windguru API errors
        if (jsonObject.has("return") && jsonObject.get("return").asString == "error") {
            val msg = jsonObject.get("error_message")?.asString ?: "Windguru API Error"
            throw RuntimeException(msg)
        }

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
        val forecastJsonElement = jsonObject.get("forecast") ?: jsonObject.get("fcst")
        val forecastJson = if (forecastJsonElement != null && forecastJsonElement.isJsonObject) {
            forecastJsonElement.asJsonObject
        } else {
            null
        }
        
        forecastJson?.entrySet()?.forEach { entry ->
            val modelId = entry.key
            val info = if (entry.value.isJsonObject) entry.value.asJsonObject else null
            
            if (info != null) {
                // In PRO mode, metrics are often arrays. info.has(...) works for both.
                if (info.has(TypeOfWeather.WINDSPD.name) || info.has(TypeOfWeather.WINDSPD.name.lowercase())) {
                    val forecast = context.deserialize<Forecast>(info, Forecast::class.java)
                    forecast.gmtHourOffset = gmtHourOffset
                    forecasts.add(ForecastModel(modelId, forecast))
                }
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
