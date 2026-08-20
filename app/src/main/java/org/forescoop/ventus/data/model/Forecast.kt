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
        get() = weathers[TypeOfWeather.WINDSPD.name]?.orderedKeys ?: emptyList()

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

    fun windDirectionName(hh: String?): String? = weathers[TypeOfWeather.WINDIRNAME.name]?.value(hh)
    fun windDirection(hh: String?): Double? = weathers[TypeOfWeather.WINDDIR.name]?.value(hh)
    fun windSpeed(hh: String?): Double? = weathers[TypeOfWeather.WINDSPD.name]?.value(hh)
    fun temperature(hh: String?): Double? = weathers[TypeOfWeather.TMP.name]?.value(hh)
    fun temperatureReal(hh: String?): Double? = weathers[TypeOfWeather.TMPE.name]?.value(hh)
    fun cloudCoverTotal(hh: String?): Int? = weathers[TypeOfWeather.TCDC.name]?.value<Double>(hh)?.toInt()
    fun cloudCoverHigh(hh: String?): Int? = weathers[TypeOfWeather.HCDC.name]?.value<Double>(hh)?.toInt()
    fun cloudCoverMid(hh: String?): Int? = weathers[TypeOfWeather.MCDC.name]?.value<Double>(hh)?.toInt()
    fun cloudCoverLow(hh: String?): Int? = weathers[TypeOfWeather.LCDC.name]?.value<Double>(hh)?.toInt()
    fun relativeHumidity(hh: String?): Int? = weathers[TypeOfWeather.RH.name]?.value<Double>(hh)?.toInt()
    
    fun windGusts(hh: String?): Double? {
        val knots = weathers[TypeOfWeather.GUST.name]?.value<Double>(hh) ?: return null
        return knots * 1.852 // conversion to kmh as in iOS
    }

    fun windGustsKnots(hh: String?): Double? = weathers[TypeOfWeather.GUST.name]?.value(hh)
    fun seaLevelPressure(hh: String?): Double? = weathers[TypeOfWeather.SLP.name]?.value(hh)
    fun freezingLevelHeightInMeters(hh: String?): Double? = weathers[TypeOfWeather.FLHGT.name]?.value(hh)

    fun windChill(hh: String?): Double? {
        val celsius = temperatureReal(hh) ?: temperature(hh) ?: return null
        val windSpeedKnots = windSpeed(hh) ?: return null
        val kmh = windSpeedKnots * 1.852
        if (celsius > 10 || kmh <= 4.8) return null
        val windFactor = kmh.pow(0.16)
        return 13.12 + 0.6215 * celsius - 11.37 * windFactor + 0.3965 * celsius * windFactor
    }

    fun precipitation(hh: String?): Double? = weathers[TypeOfWeather.APCP.name]?.value(hh)
    fun precipitation1(hh: String?): Double? = weathers[TypeOfWeather.APCP1.name]?.value(hh)

    fun waveHeight(hh: String?): Double? =
        weathers[TypeOfWeather.HTSGW.name]?.value<Double>(hh) ?: weathers[TypeOfWeather.WVHGT.name]?.value(hh)

    fun wavePeriod(hh: String?): Double? =
        weathers[TypeOfWeather.PERPW.name]?.value<Double>(hh) ?: weathers[TypeOfWeather.WVPER.name]?.value(hh)

    fun waveDirection(hh: String?): Double? =
        weathers[TypeOfWeather.DIRPW.name]?.value<Double>(hh) ?: weathers[TypeOfWeather.WVDIR.name]?.value(hh)

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
        for (entry in jsonObject.entrySet()) {
            if (entry.key != "model_name" && entry.key != "initstamp" && entry.key != "initdate") {
                if (entry.value.isJsonObject) {
                    weathers[entry.key] = context.deserialize(entry.value, TimeWeather::class.java)
                }
            }
        }
        return Forecast(initStamp, initDate, modelName, weathers)
    }
}
