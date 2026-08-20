//
//  ForecastIntelligence.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import android.content.Context
import org.forescoop.ventus.data.model.SpotForecast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

data class ForecastUnitPreferences(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KNOTS,
    val precipitationUnit: PrecipitationUnit = PrecipitationUnit.MILLIMETERS,
    val freezingLevelUnit: FreezingLevelUnit = FreezingLevelUnit.METERS,
    val pressureUnit: PressureUnit = PressureUnit.HECTOPASCALS,
    val waveHeightUnit: WaveHeightUnit = WaveHeightUnit.METERS
)

object ForecastIntelligence {

    fun summary(
        context: Context,
        forecast: SpotForecast,
        hour: String?,
        prefs: ForecastUnitPreferences = ForecastUnitPreferences(),
        locale: Locale = Locale.getDefault()
    ): String {
        return Snapshot(forecast, hour, prefs, locale).deterministicSummary()
    }

    fun detailedSummary(
        context: Context,
        forecast: SpotForecast,
        hour: String?,
        prefs: ForecastUnitPreferences = ForecastUnitPreferences(),
        locale: Locale = Locale.getDefault()
    ): String {
        return Snapshot(forecast, hour, prefs, locale).structuredOverview()
    }

    private class Snapshot(
        val forecast: SpotForecast,
        val hour: String?,
        val prefs: ForecastUnitPreferences,
        val locale: Locale
    ) {
        val selectedHour = hour ?: forecast.currentForecastHour ?: "0"
        val source = forecast.forecast
        val locationName = forecast.displayName
        val hourLabel = getHourLabel(selectedHour, forecast, locale)
        
        val tempCelsius = source?.temperatureReal(selectedHour) ?: source?.temperature(selectedHour) ?: 0.0
        val isRealTemp = source?.temperatureReal(selectedHour) != null
        
        val windKnots = source?.windSpeed(selectedHour) ?: 0.0
        val gustKnots = source?.windGustsKnots(selectedHour)
        val hasSignificantGust = gustKnots?.let { it >= windKnots + 8 } ?: false
        
        val rainMm = source?.precipitation1(selectedHour) ?: source?.precipitation(selectedHour) ?: 0.0
        val hasRain = rainMm > 0
        
        val humidity = source?.relativeHumidity(selectedHour)
        val cloudCover = source?.cloudCoverTotal(selectedHour)
        val freezingLevelM = source?.freezingLevelHeightInMeters(selectedHour)
        val pressureHpa = source?.seaLevelPressure(selectedHour)
        val waveHeightM = source?.waveHeight(selectedHour)

        fun deterministicSummary(): String {
            val tempStr = formatTemperature(tempCelsius, prefs.temperatureUnit, locale)
            val windStr = formatWind(windKnots, prefs.windSpeedUnit, locale)
            
            val phrases = mutableListOf("$tempStr with $windStr wind")
            
            phrases.add(if (isRealTemp) "real, altitude-corrected reading" else "model reading, no altitude correction available")
            
            gustKnots?.let {
                if (hasSignificantGust) {
                    phrases.add("gusting to ${formatWind(it, prefs.windSpeedUnit, locale)}")
                }
            }
            
            cloudCover?.let { phrases.add("$it% cloud cover") }
            humidity?.let { phrases.add("$it% humidity") }
            if (hasRain) {
                phrases.add("${formatPrecipitation(rainMm, prefs.precipitationUnit, locale)} of rain")
            }
            freezingLevelM?.let { phrases.add("freezing level at ${formatFreezingLevel(it, prefs.freezingLevelUnit, locale)}") }
            pressureHpa?.let { phrases.add("pressure ${formatPressure(it, prefs.pressureUnit, locale)}") }
            waveHeightM?.let { phrases.add("${formatWaveHeight(it, prefs.waveHeightUnit, locale)} waves") }
            
            // Porting outlooks
            val currentOffset = selectedHour.toIntOrNull() ?: 0
            val futureHours = forecast.availableForecastHours
                .mapNotNull { rawHour -> rawHour.toIntOrNull()?.let { rawHour to it } }
                .filter { it.second >= currentOffset }
                .sortedBy { it.second }
            
            val nextDay = futureHours.filter { it.second <= currentOffset + 24 }
            val nextTwoDays = futureHours.filter { it.second <= currentOffset + 48 }

            getWindOutlook(nextDay, forecast, prefs.windSpeedUnit, locale)?.let { phrases.add(it) }
            getTemperatureOutlook(nextTwoDays, forecast, prefs.temperatureUnit, locale)?.let { phrases.add(it) }
            getPrecipitationOutlook(nextDay, forecast, prefs.precipitationUnit, locale)?.let { phrases.add(it) }
            getWaveOutlook(nextTwoDays, forecast, prefs.waveHeightUnit, locale)?.let { phrases.add(it) }

            val body = phrases.joinToString(", ") + "."
            return "$locationName · $hourLabel: $body"
        }

        fun structuredOverview(): String {
            val phrases = mutableListOf(
                formatTemperature(tempCelsius, prefs.temperatureUnit, locale) + " with " + formatWind(windKnots, prefs.windSpeedUnit, locale) + " wind"
            )
            if (hasSignificantGust) {
                gustKnots?.let { phrases.add("gusting to ${formatWind(it, prefs.windSpeedUnit, locale)}") }
            }
            if (hasRain) {
                phrases.add("${formatPrecipitation(rainMm, prefs.precipitationUnit, locale)} of rain")
            }
            cloudCover?.let { phrases.add("$it% cloud cover") }
            humidity?.let { phrases.add("$it% humidity") }
            freezingLevelM?.let { phrases.add("freezing level at ${formatFreezingLevel(it, prefs.freezingLevelUnit, locale)}") }
            pressureHpa?.let { phrases.add("pressure ${formatPressure(it, prefs.pressureUnit, locale)}") }
            waveHeightM?.let { phrases.add("${formatWaveHeight(it, prefs.waveHeightUnit, locale)} waves") }
            
            val openingBody = phrases.joinToString(", ")
            val opening = "For $hourLabel, expect $openingBody."

            val currentOffset = selectedHour.toIntOrNull() ?: 0
            val futureHours = forecast.availableForecastHours
                .mapNotNull { rawHour -> rawHour.toIntOrNull()?.let { rawHour to it } }
                .filter { it.second >= currentOffset }
                .sortedBy { it.second }
            
            val days = getDayDetails(futureHours, forecast, locale)
            if (days.isEmpty()) return opening

            val windOutlook = getWindOutlook(futureHours.filter { it.second <= currentOffset + 24 }, forecast, prefs.windSpeedUnit, locale)
            val waveOutlook = getWaveOutlook(futureHours.filter { it.second <= currentOffset + 48 }, forecast, prefs.waveHeightUnit, locale)
            val precipitationOutlook = getPrecipitationOutlook(futureHours.filter { it.second <= currentOffset + 24 }, forecast, prefs.precipitationUnit, locale)

            val precipitationDays = days.filter { it.precipitationMm > 0 }
            val hasUnsettledPattern = precipitationDays.size >= maxOf(3, days.size / 2)
            val coldestDay = days.minByOrNull { it.lowCelsius }
            val hottestDay = days.maxByOrNull { it.highCelsius }
            
            var iceRiskDay: DayDetail? = null
            for (i in 0 until days.size - 1) {
                if (days[i].precipitationMm > 0 && days[i + 1].lowCelsius <= 0) {
                    iceRiskDay = days[i + 1]
                    break
                }
            }

            val points = mutableListOf<Pair<String, String>>()

            windOutlook?.let {
                val capitalized = it.replaceFirstChar { char -> char.uppercase() }
                val chillNote = "Winds at that strength can make it feel noticeably colder than the air temperature alone suggests."
                points.add("Wind & Chill" to "$capitalized. $chillNote")
            }

            waveOutlook?.let {
                val capitalized = it.replaceFirstChar { char -> char.uppercase() }
                points.add("Sea & Swell" to "$capitalized.")
            }

            iceRiskDay?.let {
                val body = "Wet surfaces from rain, combined with a low of ${formatTemperature(it.lowCelsius, prefs.temperatureUnit, locale)} around ${it.label}, can refreeze into ice on exposed paths and roads overnight."
                points.add("Ice Hazard" to body)
            }

            if (hasUnsettledPattern) {
                val kind = if (precipitationDays.any { it.isSnow }) "rain or snow" else "rain"
                val body = "${precipitationDays.size} of the next ${days.size} days bring $kind, so expect a wet, changeable stretch rather than a single passing system."
                points.add("Unsettled Outlook" to body)
            } else if (precipitationOutlook != null) {
                val capitalized = precipitationOutlook.replaceFirstChar { char -> char.uppercase() }
                points.add("Precipitation" to "$capitalized.")
            }

            coldestDay?.let {
                if (it.lowCelsius <= -5) {
                    val body = "Overnight lows drop to around ${formatTemperature(it.lowCelsius, prefs.temperatureUnit, locale)} on ${it.label} — dress for a sharp chill after dark."
                    points.add("Cold Snap" to body)
                }
            }

            hottestDay?.let {
                if (it.highCelsius >= 32) {
                    val body = "Highs climb to around ${formatTemperature(it.highCelsius, prefs.temperatureUnit, locale)} on ${it.label}, well above the rest of the outlook."
                    points.add("Heat" to body)
                }
            }

            if (points.isEmpty()) {
                val outlook = getDailyOutlook(days, prefs.temperatureUnit, locale)
                return if (outlook != null) "$opening\n\nNo notable hazards ahead — a generally quiet stretch: $outlook." else opening
            }

            val sections = points.joinToString("\n\n") { "**${it.first}:** ${it.second}" }
            return "$opening\n\n**Key Trends & Impact**\n\n$sections"
        }

        private data class DayDetail(
            val label: String,
            val lowCelsius: Double,
            val highCelsius: Double,
            val precipitationMm: Double,
            val isSnow: Boolean,
            val peakBft: Int,
            val condition: String
        )

        private fun getDayDetails(
            hours: List<Pair<String, Int>>,
            forecast: SpotForecast,
            locale: Locale
        ): List<DayDetail> {
            val source = forecast.forecast ?: return emptyList()
            val grouped = hours.groupBy { item ->
                val date = forecast.forecastDate(item.first) ?: Date()
                Calendar.getInstance().apply { time = date }.let {
                    it.set(Calendar.HOUR_OF_DAY, 0)
                    it.set(Calendar.MINUTE, 0)
                    it.set(Calendar.SECOND, 0)
                    it.set(Calendar.MILLISECOND, 0)
                    it.time
                }
            }
            
            val sortedDays = grouped.keys.sorted().take(10)
            val now = Calendar.getInstance()

            return sortedDays.mapNotNull { dayDate ->
                val dayHours = grouped[dayDate] ?: return@mapNotNull null
                val temps = dayHours.mapNotNull { source.temperatureReal(it.first) ?: source.temperature(it.first) }
                if (temps.isEmpty()) return@mapNotNull null
                
                val low = temps.min()
                val high = temps.max()
                
                var precipTotal = 0.0
                var isSnowDay = false
                var peakBft = 0
                
                for (item in dayHours) {
                    val amount = (source.precipitation1(item.first) ?: source.precipitation(item.first)) ?: 0.0
                    precipTotal += amount
                    if (amount > 0) {
                        val celsius = source.temperatureReal(item.first) ?: source.temperature(item.first) ?: 0.0
                        if (celsius <= 0) isSnowDay = true
                    }
                    val windKts = source.windSpeed(item.first) ?: 0.0
                    val gustKts = source.windGustsKnots(item.first) ?: 0.0
                    peakBft = maxOf(peakBft, Knots(windKts).bft, Knots(gustKts).bft)
                }
                
                val representativeHour = dayHours[dayHours.size / 2].first
                val condition = getConditionWord(forecast, representativeHour, locale)
                
                val dayCal = Calendar.getInstance().apply { time = dayDate }
                val label = if (now.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)) {
                    "today"
                } else {
                    SimpleDateFormat("EEE d", locale).format(dayDate)
                }
                
                DayDetail(label, low, high, precipTotal, isSnowDay, peakBft, condition)
            }
        }

        private fun getDailyOutlook(days: List<DayDetail>, temperatureUnit: TemperatureUnit, locale: Locale): String? {
            if (days.size <= 1) return null
            return days.joinToString(", ") { day ->
                "${day.label} ${formatTemperature(day.lowCelsius, temperatureUnit, locale)}–${formatTemperature(day.highCelsius, temperatureUnit, locale)} ${day.condition}"
            }
        }

        private fun getConditionWord(forecast: SpotForecast, hour: String, locale: Locale): String {
            val symbols = forecast.weatherSymbolNames(hour)
            return when {
                symbols.any { it.contains("snow") } -> "snow"
                symbols.any { it.contains("rain") || it.contains("drop") || it.contains("umbrella") } -> "rain"
                symbols.any { it.contains("fog") } -> "fog"
                symbols.contains("cloud") -> "overcast"
                symbols.any { it.contains("cloudy") } -> "partly cloudy"
                else -> "clear"
            }
        }

        private fun getWindOutlook(
            hours: List<Pair<String, Int>>,
            forecast: SpotForecast,
            unit: WindSpeedUnit,
            locale: Locale
        ): String? {
            val source = forecast.forecast ?: return null
            var peakBft = 0
            var peakKnots = 0.0
            var peakHour: String? = null
            
            for (item in hours) {
                val windKts = source.windSpeed(item.first) ?: 0.0
                val gustKts = source.windGustsKnots(item.first) ?: 0.0
                val bft = maxOf(Knots(windKts).bft, Knots(gustKts).bft)
                val strongest = maxOf(windKts, gustKts)
                if (bft > peakBft || (bft == peakBft && strongest > peakKnots)) {
                    peakBft = bft
                    peakKnots = strongest
                    peakHour = item.first
                }
            }
            
            if (peakBft >= 6 && peakHour != null) {
                val label = getHourLabel(peakHour, forecast, locale)
                return "strong wind expected, up to ${formatWind(peakKnots, unit, locale)} (Beaufort $peakBft) around $label"
            }
            return null
        }

        private fun getTemperatureOutlook(
            hours: List<Pair<String, Int>>,
            forecast: SpotForecast,
            unit: TemperatureUnit,
            locale: Locale
        ): String? {
            val source = forecast.forecast ?: return null
            var coldest: Pair<Double, String>? = null
            var hottest: Pair<Double, String>? = null
            
            for (item in hours) {
                val celsius = source.temperatureReal(item.first) ?: source.temperature(item.first) ?: continue
                if (coldest == null || celsius < coldest!!.first) coldest = celsius to item.first
                if (hottest == null || celsius > hottest!!.first) hottest = celsius to item.first
            }
            
            coldest?.let {
                if (it.first <= -5) {
                    val label = getHourLabel(it.second, forecast, locale)
                    return "very cold, dropping to ${formatTemperature(it.first, unit, locale)} around $label"
                }
            }
            hottest?.let {
                if (it.first >= 32) {
                    val label = getHourLabel(it.second, forecast, locale)
                    return "very hot, reaching ${formatTemperature(it.first, unit, locale)} around $label"
                }
            }
            return null
        }

        private fun getPrecipitationOutlook(
            hours: List<Pair<String, Int>>,
            forecast: SpotForecast,
            unit: PrecipitationUnit,
            locale: Locale
        ): String? {
            val source = forecast.forecast ?: return null
            var total = 0.0
            var isSnow = false
            var onsetHour: String? = null
            
            for (item in hours) {
                val amount = (source.precipitation1(item.first) ?: source.precipitation(item.first)) ?: 0.0
                if (amount > 0) {
                    total += amount
                    if (onsetHour == null) onsetHour = item.first
                    val celsius = source.temperatureReal(item.first) ?: source.temperature(item.first) ?: 0.0
                    if (celsius <= 0) isSnow = true
                }
            }
            
            if (total > 0) {
                val amountText = formatPrecipitation(total, unit, locale)
                val kind = if (isSnow) "snow" else "rain"
                onsetHour?.let {
                    val label = getHourLabel(it, forecast, locale)
                    return "$amountText of $kind expected, starting around $label"
                }
                return "$amountText of $kind expected"
            }
            return null
        }

        private fun getWaveOutlook(
            hours: List<Pair<String, Int>>,
            forecast: SpotForecast,
            unit: WaveHeightUnit,
            locale: Locale
        ): String? {
            val source = forecast.forecast ?: return null
            var peakMeters = 0.0
            var peakHour: String? = null
            
            for (item in hours) {
                val meters = source.waveHeight(item.first) ?: continue
                if (meters > peakMeters) {
                    peakMeters = meters
                    peakHour = item.first
                }
            }
            
            if (peakMeters >= 2.5 && peakHour != null) {
                val label = getHourLabel(peakHour, forecast, locale)
                return "large swell expected, up to ${formatWaveHeight(peakMeters, unit, locale)} around $label"
            }
            return null
        }

        private fun getHourLabel(hour: String, forecast: SpotForecast, locale: Locale): String {
            val date = forecast.forecastDate(hour) ?: return String.format("%02d:00", (hour.toIntOrNull() ?: 0) % 24)
            val now = Calendar.getInstance()
            val forecastCal = Calendar.getInstance().apply { time = date }
            
            val isToday = now.get(Calendar.YEAR) == forecastCal.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == forecastCal.get(Calendar.DAY_OF_YEAR)
            
            val dayPart = if (isToday) "today" else SimpleDateFormat("EEE d", locale).format(date)
            val timePart = SimpleDateFormat("HH:mm", locale).format(date)
            
            return "$dayPart, $timePart"
        }

        private fun formatTemperature(celsius: Double, unit: TemperatureUnit, locale: Locale): String {
            val value = Temperature(celsius).value(unit)
            return String.format(locale, "%.0f%s", value, unit.label)
        }

        private fun formatWind(knots: Double, unit: WindSpeedUnit, locale: Locale): String {
            val value = Knots(knots).value(unit)
            return String.format(locale, "%.0f %s", value, unit.label)
        }

        private fun formatPrecipitation(mm: Double, unit: PrecipitationUnit, locale: Locale): String {
            val value = Precipitation(mm).value(unit)
            return String.format(locale, "%.0f %s", value, unit.label)
        }

        private fun formatFreezingLevel(meters: Double, unit: FreezingLevelUnit, locale: Locale): String {
            val value = FreezingLevel(meters).value(unit)
            return String.format(locale, "%.0f %s", value, unit.label)
        }

        private fun formatPressure(hpa: Double, unit: PressureUnit, locale: Locale): String {
            val value = AtmosphericPressure(hpa).value(unit)
            val precision = if (unit == PressureUnit.INCHES_OF_MERCURY) 2 else 0
            return String.format(locale, "%.${precision}f %s", value, unit.label)
        }

        private fun formatWaveHeight(meters: Double, unit: WaveHeightUnit, locale: Locale): String {
            val value = WaveHeight(meters).value(unit)
            return String.format(locale, "%.1f %s", value, unit.label)
        }
    }
}
