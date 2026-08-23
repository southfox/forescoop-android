//
//  Forecast.kt
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
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import kotlin.math.abs
import kotlin.math.pow

enum class TypeOfWeather {
    TMP, TCDC, HCDC, MCDC, LCDC, RH, GUST, SLP, FLHGT, APCP, APCP1,
    WINDSPD, WINDDIR, WINDIRNAME, TMPE, HTSGW, WVHGT, WVPER, WVDIR,
    PERPW, DIRPW
}

@JsonAdapter(ForecastDeserializer::class)
class Forecast(
    @SerializedName("initstamp") val initStamp: Long = 0,
    @SerializedName("initdate") val initDate: String? = null,
    @SerializedName("model_name") val modelName: String? = null,
    val weathers: Map<String, TimeWeather> = emptyMap()
) {
    var gmtHourOffset: Int = 0

    val availableHours: List<String>
        get() {
            val weather = weathers[TypeOfWeather.WINDSPD.name] ?: weathers[TypeOfWeather.WINDSPD.name.lowercase()]
            return weather?.orderedKeys ?: emptyList()
        }

    val stepIntervalHours: Int
        get() {
            val offsets = availableHours.mapNotNull { it.toIntOrNull() }.sorted()
            if (offsets.size <= 1) return 1
            val intervals = offsets.zipWithNext { a, b -> b - a }
            return intervals.reduce { acc, i -> greatestCommonDivisor(acc, i) }
        }

    private fun greatestCommonDivisor(a: Int, b: Int): Int {
        var x = abs(a)
        var y = abs(b)
        while (y != 0) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }

    private fun <T> getWeatherValue(type: TypeOfWeather, hh: String?): T? {
        val weather = weathers[type.name] ?: weathers[type.name.lowercase()]
        return weather?.value(hh)
    }

    fun windDirectionName(hh: String?): String? = getWeatherValue(TypeOfWeather.WINDIRNAME, hh)
    fun windDirection(hh: String?): Double? = getWeatherValue(TypeOfWeather.WINDDIR, hh)
    fun windSpeed(hh: String?): Double? = getWeatherValue(TypeOfWeather.WINDSPD, hh)
    fun temperature(hh: String?): Double? = getWeatherValue(TypeOfWeather.TMP, hh)
    fun temperatureReal(hh: String?): Double? = getWeatherValue(TypeOfWeather.TMPE, hh)
    fun cloudCoverTotal(hh: String?): Int? = getWeatherValue<Double>(TypeOfWeather.TCDC, hh)?.toInt()
    fun cloudCoverHigh(hh: String?): Int? = getWeatherValue<Double>(TypeOfWeather.HCDC, hh)?.toInt()
    fun cloudCoverMid(hh: String?): Int? = getWeatherValue<Double>(TypeOfWeather.MCDC, hh)?.toInt()
    fun cloudCoverLow(hh: String?): Int? = getWeatherValue<Double>(TypeOfWeather.LCDC, hh)?.toInt()
    fun relativeHumidity(hh: String?): Int? = getWeatherValue<Double>(TypeOfWeather.RH, hh)?.toInt()
    
    fun windGusts(hh: String?): Double? {
        val knots = windGustsKnots(hh) ?: return null
        return knots * 1.852 // conversion to kmh as in iOS
    }

    fun windGustsKnots(hh: String?): Double? = getWeatherValue(TypeOfWeather.GUST, hh)
    fun seaLevelPressure(hh: String?): Double? = getWeatherValue(TypeOfWeather.SLP, hh)
    fun freezingLevelHeightInMeters(hh: String?): Double? = getWeatherValue(TypeOfWeather.FLHGT, hh)

    fun windChill(hh: String?): Double? {
        val celsius = temperatureReal(hh) ?: temperature(hh) ?: return null
        val windSpeedKnots = windSpeed(hh) ?: return null
        val kmh = windSpeedKnots * 1.852
        if (celsius > 10 || kmh <= 4.8) return null
        val windFactor = kmh.pow(0.16)
        return 13.12 + 0.6215 * celsius - 11.37 * windFactor + 0.3965 * celsius * windFactor
    }

    fun precipitation(hh: String?): Double? = getWeatherValue(TypeOfWeather.APCP, hh)
    fun precipitation1(hh: String?): Double? = getWeatherValue(TypeOfWeather.APCP1, hh)

    fun waveHeight(hh: String?): Double? =
        getWeatherValue<Double>(TypeOfWeather.HTSGW, hh) ?: getWeatherValue(TypeOfWeather.WVHGT, hh)

    fun wavePeriod(hh: String?): Double? =
        getWeatherValue<Double>(TypeOfWeather.PERPW, hh) ?: getWeatherValue(TypeOfWeather.WVPER, hh)

    fun waveDirection(hh: String?): Double? =
        getWeatherValue<Double>(TypeOfWeather.DIRPW, hh) ?: getWeatherValue(TypeOfWeather.WVDIR, hh)

    override fun toString(): String {
        return "Forecast\n$initStamp\n$initDate\n$modelName\n" +
                weathers.entries.joinToString("\n") { "${it.key}: {${it.value}}" }
    }
}

class ForecastDeserializer : JsonDeserializer<Forecast> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Forecast {
        val jsonObject = json.asJsonObject
        val initStamp = jsonObject.get("initstamp")?.asLong ?: 0
        val initDate = jsonObject.get("initdate")?.asString
        val modelName = jsonObject.get("model_name")?.asString
        
        val weathers = mutableMapOf<String, TimeWeather>()
        
        // Check if we have an "hours" array (typical for PRO/wforecast responses)
        val hoursArray = jsonObject.get("hours")?.asJsonArray
        val hourStrings = hoursArray?.map { it.asString }
        
        for (entry in jsonObject.entrySet()) {
            val key = entry.key.uppercase()
            if (key == "MODEL_NAME" || key == "INITSTAMP" || key == "INITDATE" || key == "HOURS") continue
            
            val value = entry.value
            when {
                value.isJsonObject -> {
                    weathers[key] = context.deserialize(value, TimeWeather::class.java)
                }
                value.isJsonArray && hourStrings != null -> {
                    // Convert array to TimeWeather using hour offsets
                    val array = value.asJsonArray
                    val info = mutableMapOf<String, Any>()
                    for (i in 0 until minOf(array.size(), hourStrings.size)) {
                        val element = array.get(i)
                        if (element.isJsonPrimitive) {
                            val primitive = element.asJsonPrimitive
                            when {
                                primitive.isNumber -> info[hourStrings[i]] = primitive.asDouble
                                primitive.isBoolean -> info[hourStrings[i]] = primitive.asBoolean
                                primitive.isString -> info[hourStrings[i]] = primitive.asString
                            }
                        }
                    }
                    weathers[key] = TimeWeather(info)
                }
            }
        }
        return Forecast(initStamp, initDate, modelName, weathers)
    }
}
