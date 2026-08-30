//
//  ForecastMoonCalendar.kt
//  Ventus
//

package org.forescoop.ventus.data.model

import org.forescoop.ventus.R
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonTimes
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ForecastMoonCalendar {
    private val cache = ConcurrentHashMap<String, ForecastMoonInfo>()

    fun info(forecast: SpotForecast, onDate: Date): ForecastMoonInfo? {
        if (forecast.latitude == 0.0 && forecast.longitude == 0.0) return null
        if (forecast.latitude !in -90.0..90.0 || forecast.longitude !in -180.0..180.0) return null

        val tz = if (forecast.tz != null) TimeZone.getTimeZone(forecast.tz) else TimeZone.getDefault()
        val calendar = Calendar.getInstance(tz).apply { 
            time = onDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day = calendar.time
        
        val key = String.format(Locale.US, "%.3f,%.3f,%d", forecast.latitude, forecast.longitude, day.time)
        cache[key]?.let { return it }

        try {
            val parameters = MoonIllumination.compute()
                .on(day)
                .timezone(tz.toZoneId())
                .latitude(forecast.latitude)
                .longitude(forecast.longitude)
                .execute()

            val illumination = parameters.fraction
            val phaseAngle = parameters.phase // 0 to 360

            val times = MoonTimes.compute()
                .on(day)
                .timezone(tz.toZoneId())
                .latitude(forecast.latitude)
                .longitude(forecast.longitude)
                .execute()

            val moonrise = times.rise?.let { if (isSameDay(it, day, tz)) Date.from(it.toInstant()) else null }
            val moonset = times.set?.let { if (isSameDay(it, day, tz)) Date.from(it.toInstant()) else null }

            val info = ForecastMoonInfo(
                day = day,
                phaseNameResId = getPhaseResId(phaseAngle),
                illumination = illumination,
                symbolName = getPhaseSymbol(phaseAngle),
                moonrise = moonrise,
                moonset = moonset
            )
            cache[key] = info
            return info
        } catch (e: Exception) {
            return null
        }
    }

    private fun isSameDay(zdt: ZonedDateTime, date2: Date, tz: TimeZone): Boolean {
        val cal1 = Calendar.getInstance(tz).apply { time = Date.from(zdt.toInstant()) }
        val cal2 = Calendar.getInstance(tz).apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getPhaseResId(angle: Double): Int {
        val normalized = (angle % 360 + 360) % 360
        return when (normalized) {
            in 0.0..11.25, in 348.75..360.0 -> R.string.moon_new
            in 11.25..78.75 -> R.string.moon_waxing_crescent
            in 78.75..101.25 -> R.string.moon_first_quarter
            in 101.25..168.75 -> R.string.moon_waxing_gibbous
            in 168.75..191.25 -> R.string.moon_full
            in 191.25..258.75 -> R.string.moon_waning_gibbous
            in 258.75..281.25 -> R.string.moon_last_quarter
            else -> R.string.moon_waning_crescent
        }
    }

    private fun getPhaseSymbol(angle: Double): String {
        val normalized = (angle % 360 + 360) % 360
        return when (normalized) {
            in 0.0..22.5, in 337.5..360.0 -> "moonphase.new.moon"
            in 22.5..67.5 -> "moonphase.waxing.crescent"
            in 67.5..112.5 -> "moonphase.first.quarter"
            in 112.5..157.5 -> "moonphase.waxing.gibbous"
            in 157.5..202.5 -> "moonphase.full.moon"
            in 202.5..247.5 -> "moonphase.waning.gibbous"
            in 247.5..292.5 -> "moonphase.last.quarter"
            else -> "moonphase.waning.crescent"
        }
    }

    fun events(forecast: SpotForecast, start: Date, end: Date): List<Pair<Date, String>> {
        if (start >= end) return emptyList()
        val tz = if (forecast.tz != null) TimeZone.getTimeZone(forecast.tz) else TimeZone.getDefault()
        val calendar = Calendar.getInstance(tz).apply { time = start }
        val events = mutableListOf<Pair<Date, String>>()
        
        var current = calendar.time
        while (current < end) {
            info(forecast, current)?.let { info ->
                info.moonrise?.let { if (it >= start && it < end) events.add(it to "moonrise") }
                info.moonset?.let { if (it >= start && it < end) events.add(it to "moonset") }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            current = calendar.time
        }
        return events.sortedBy { it.first }
    }
}
