//
//  ForecastIntelligence.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import android.content.Context
import org.forescoop.ventus.R
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
        return Snapshot(context, forecast, hour, prefs, locale).deterministicSummary()
    }

    fun detailedSummary(
        context: Context,
        forecast: SpotForecast,
        hour: String?,
        prefs: ForecastUnitPreferences = ForecastUnitPreferences(),
        locale: Locale = Locale.getDefault()
    ): String {
        return Snapshot(context, forecast, hour, prefs, locale).structuredOverview()
    }

    private class Snapshot(
        val context: Context,
        val forecast: SpotForecast,
        val hour: String?,
        val prefs: ForecastUnitPreferences,
        val locale: Locale
    ) {
        val selectedHour = hour ?: forecast.currentForecastHour ?: "0"
        val source = forecast.forecast
        val locationName = forecast.displayName
        val hourLabel = getHourLabel(context, selectedHour, forecast, locale)
        
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
            
            val phrases = mutableListOf(context.getString(R.string.summary_temp_with_wind, tempStr, windStr))
            
            phrases.add(if (isRealTemp) context.getString(R.string.summary_real_temp) else context.getString(R.string.summary_model_temp))
            
            gustKnots?.let {
                if (hasSignificantGust) {
                    phrases.add(context.getString(R.string.summary_gusting, formatWind(it, prefs.windSpeedUnit, locale)))
                }
            }
            
            cloudCover?.let { phrases.add(context.getString(R.string.summary_cloud_cover, it)) }
            humidity?.let { phrases.add(context.getString(R.string.summary_humidity, it)) }
            if (hasRain) {
                val kind = if (forecast.weatherSymbolNames(selectedHour).any { it.contains("snow") }) context.getString(R.string.snow) else context.getString(R.string.rain)
                phrases.add(context.getString(R.string.summary_rain, formatPrecipitation(rainMm, prefs.precipitationUnit, locale), kind))
            }
            freezingLevelM?.let { phrases.add(context.getString(R.string.summary_freezing_level, formatFreezingLevel(it, prefs.freezingLevelUnit, locale))) }
            pressureHpa?.let { phrases.add(context.getString(R.string.summary_pressure, formatPressure(it, prefs.pressureUnit, locale))) }
            waveHeightM?.let { phrases.add(context.getString(R.string.summary_waves, formatWaveHeight(it, prefs.waveHeightUnit, locale))) }
            
            // Porting outlooks
            val currentOffset = selectedHour.toIntOrNull() ?: 0
            val futureHours = forecast.availableForecastHours
                .mapNotNull { rawHour -> rawHour.toIntOrNull()?.let { rawHour to it } }
                .filter { it.second >= currentOffset }
                .sortedBy { it.second }
            
            val nextDay = futureHours.filter { it.second <= currentOffset + 24 }
            val nextTwoDays = futureHours.filter { it.second <= currentOffset + 48 }

            getWindOutlook(context, nextDay, forecast, prefs.windSpeedUnit, locale)?.let { phrases.add(it) }
            getTemperatureOutlook(context, nextTwoDays, forecast, prefs.temperatureUnit, locale)?.let { phrases.add(it) }
            getPrecipitationOutlook(context, nextDay, forecast, prefs.precipitationUnit, locale)?.let { phrases.add(it) }
            getWaveOutlook(context, nextTwoDays, forecast, prefs.waveHeightUnit, locale)?.let { phrases.add(it) }

            val body = phrases.joinToString(", ") + "."
            return "$locationName · $hourLabel: $body"
        }

        fun structuredOverview(): String {
            val phrases = mutableListOf<String>()
            val tempStr = formatTemperature(tempCelsius, prefs.temperatureUnit, locale)
            val windStr = formatWind(windKnots, prefs.windSpeedUnit, locale)
            phrases.add(context.getString(R.string.summary_temp_with_wind, tempStr, windStr))

            if (hasSignificantGust) {
                gustKnots?.let { phrases.add(context.getString(R.string.summary_gusting, formatWind(it, prefs.windSpeedUnit, locale))) }
            }
            if (hasRain) {
                val kind = if (forecast.weatherSymbolNames(selectedHour).any { it.contains("snow") }) context.getString(R.string.snow) else context.getString(R.string.rain)
                phrases.add(context.getString(R.string.summary_rain, formatPrecipitation(rainMm, prefs.precipitationUnit, locale), kind))
            }
            cloudCover?.let { phrases.add(context.getString(R.string.summary_cloud_cover, it)) }
            humidity?.let { phrases.add(context.getString(R.string.summary_humidity, it)) }
            freezingLevelM?.let { phrases.add(context.getString(R.string.summary_freezing_level, formatFreezingLevel(it, prefs.freezingLevelUnit, locale))) }
            pressureHpa?.let { phrases.add(context.getString(R.string.summary_pressure, formatPressure(it, prefs.pressureUnit, locale))) }
            waveHeightM?.let { phrases.add(context.getString(R.string.summary_waves, formatWaveHeight(it, prefs.waveHeightUnit, locale))) }
            
            val openingBody = phrases.joinToString(", ")
            val opening = context.getString(R.string.summary_expect, hourLabel, openingBody)

            val currentOffset = selectedHour.toIntOrNull() ?: 0
            val futureHours = forecast.availableForecastHours
                .mapNotNull { rawHour -> rawHour.toIntOrNull()?.let { rawHour to it } }
                .filter { it.second >= currentOffset }
                .sortedBy { it.second }
            
            val days = getDayDetails(context, futureHours, forecast, locale)
            if (days.isEmpty()) return opening

            val windOutlook = getWindOutlook(context, futureHours.filter { it.second <= currentOffset + 24 }, forecast, prefs.windSpeedUnit, locale)
            val waveOutlook = getWaveOutlook(context, futureHours.filter { it.second <= currentOffset + 48 }, forecast, prefs.waveHeightUnit, locale)
            val precipitationOutlook = getPrecipitationOutlook(context, futureHours.filter { it.second <= currentOffset + 24 }, forecast, prefs.precipitationUnit, locale)

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
                val chillNote = context.getString(R.string.chill_note)
                points.add(context.getString(R.string.wind_and_chill_title) to "$capitalized. $chillNote")
            }

            waveOutlook?.let {
                val capitalized = it.replaceFirstChar { char -> char.uppercase() }
                points.add(context.getString(R.string.sea_and_swell_title) to "$capitalized.")
            }

            iceRiskDay?.let {
                val body = context.getString(R.string.ice_risk_body, formatTemperature(it.lowCelsius, prefs.temperatureUnit, locale), it.label)
                points.add(context.getString(R.string.ice_hazard_title) to body)
            }

            if (hasUnsettledPattern) {
                val kind = if (precipitationDays.any { it.isSnow }) context.getString(R.string.rain_or_snow) else context.getString(R.string.rain)
                val body = context.getString(R.string.unsettled_body, precipitationDays.size, days.size, kind)
                points.add(context.getString(R.string.unsettled_outlook_title) to body)
            } else if (precipitationOutlook != null) {
                val capitalized = precipitationOutlook.replaceFirstChar { char -> char.uppercase() }
                points.add(context.getString(R.string.precipitation) to "$capitalized.")
            }

            coldestDay?.let {
                if (it.lowCelsius <= -5) {
                    val body = context.getString(R.string.cold_snap_body, formatTemperature(it.lowCelsius, prefs.temperatureUnit, locale), it.label)
                    points.add(context.getString(R.string.cold_snap_title) to body)
                }
            }

            hottestDay?.let {
                if (it.highCelsius >= 32) {
                    val body = context.getString(R.string.heat_body, formatTemperature(it.highCelsius, prefs.temperatureUnit, locale), it.label)
                    points.add(context.getString(R.string.heat_title) to body)
                }
            }

            if (points.isEmpty()) {
                val outlook = getDailyOutlook(context, days, prefs.temperatureUnit, locale)
                return if (outlook != null) context.getString(R.string.summary_no_hazards, outlook) else opening
            }

            val sections = points.joinToString("\n\n") { "**${it.first}:** ${it.second}" }
            return "$opening\n\n**" + context.getString(R.string.key_trends_title) + "**\n\n$sections"
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
            context: Context,
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
                val condition = getConditionWord(context, forecast, representativeHour, locale)
                
                val dayCal = Calendar.getInstance().apply { time = dayDate }
                val label = if (now.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)) {
                    context.getString(R.string.today)
                } else {
                    SimpleDateFormat("EEE d", locale).format(dayDate)
                }
                
                DayDetail(label, low, high, precipTotal, isSnowDay, peakBft, condition)
            }
        }

        private fun getDailyOutlook(context: Context, days: List<DayDetail>, temperatureUnit: TemperatureUnit, locale: Locale): String? {
            if (days.size <= 1) return null
            return days.joinToString(", ") { day ->
                "${day.label} ${formatTemperature(day.lowCelsius, temperatureUnit, locale)}–${formatTemperature(day.highCelsius, temperatureUnit, locale)} ${day.condition}"
            }
        }

        private fun getConditionWord(context: Context, forecast: SpotForecast, hour: String, locale: Locale): String {
            val symbols = forecast.weatherSymbolNames(hour)
            return when {
                symbols.any { it.contains("snow") } -> context.getString(R.string.snow)
                symbols.any { it.contains("rain") || it.contains("drop") || it.contains("umbrella") } -> context.getString(R.string.rain)
                symbols.any { it.contains("fog") } -> context.getString(R.string.fog)
                symbols.contains("cloud") -> context.getString(R.string.overcast)
                symbols.any { it.contains("cloudy") } -> context.getString(R.string.partly_cloudy)
                else -> context.getString(R.string.clear)
            }
        }

        private fun getWindOutlook(
            context: Context,
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
                val label = getHourLabel(context, peakHour, forecast, locale)
                return context.getString(R.string.summary_strong_wind, formatWind(peakKnots, unit, locale), peakBft, label)
            }
            return null
        }

        private fun getTemperatureOutlook(
            context: Context,
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
                    val label = getHourLabel(context, it.second, forecast, locale)
                    return context.getString(R.string.summary_very_cold, formatTemperature(it.first, unit, locale), label)
                }
            }
            hottest?.let {
                if (it.first >= 32) {
                    val label = getHourLabel(context, it.second, forecast, locale)
                    return context.getString(R.string.summary_very_hot, formatTemperature(it.first, unit, locale), label)
                }
            }
            return null
        }

        private fun getPrecipitationOutlook(
            context: Context,
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
                val kind = if (isSnow) context.getString(R.string.snow) else context.getString(R.string.rain)
                onsetHour?.let {
                    val label = getHourLabel(context, it, forecast, locale)
                    return context.getString(R.string.summary_precip_expected, amountText, kind, label)
                }
                return context.getString(R.string.summary_precip_expected_simple, amountText, kind)
            }
            return null
        }

        private fun getWaveOutlook(
            context: Context,
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
                val label = getHourLabel(context, peakHour, forecast, locale)
                return context.getString(R.string.summary_large_swell, formatWaveHeight(peakMeters, unit, locale), label)
            }
            return null
        }

        private fun getHourLabel(context: Context, hour: String, forecast: SpotForecast, locale: Locale): String {
            val date = forecast.forecastDate(hour) ?: return String.format("%02d:00", (hour.toIntOrNull() ?: 0) % 24)
            val now = Calendar.getInstance()
            val forecastCal = Calendar.getInstance().apply { time = date }
            
            val isToday = now.get(Calendar.YEAR) == forecastCal.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == forecastCal.get(Calendar.DAY_OF_YEAR)
            
            val dayPart = if (isToday) context.getString(R.string.today) else SimpleDateFormat("EEE d", locale).format(date)
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
