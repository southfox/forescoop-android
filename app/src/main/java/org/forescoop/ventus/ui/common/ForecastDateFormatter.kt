//
//  ForecastDateFormatter.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-23.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import android.content.Context
import android.text.format.DateFormat
import org.forescoop.ventus.data.model.SpotForecast
import java.text.SimpleDateFormat
import java.util.*

object ForecastDateFormatter {
    fun formatHour(context: Context, date: Date?, timeZone: String? = null): String {
        if (date == null) return "--"
        val tz = if (timeZone != null) TimeZone.getTimeZone(timeZone) else TimeZone.getDefault()
        
        val calendar = Calendar.getInstance(tz).apply { time = date }
        val is24Hour = DateFormat.is24HourFormat(context)
        val hasMinutes = calendar.get(Calendar.MINUTE) != 0
        
        val pattern = when {
            is24Hour && hasMinutes -> "H:mm"
            is24Hour -> "H"
            !is24Hour && hasMinutes -> "h:mm a"
            else -> "h a"
        }
        
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.timeZone = tz
        val formatted = sdf.format(date)
        
        return if (is24Hour && !hasMinutes) formatted + "h" else formatted
    }

    fun formatDay(date: Date?, timeZone: String? = null): String {
        if (date == null) return ""
        val tz = if (timeZone != null) TimeZone.getTimeZone(timeZone) else TimeZone.getDefault()
        val sdf = SimpleDateFormat("EEE d", Locale.getDefault())
        sdf.timeZone = tz
        return sdf.format(date)
    }

    fun isToday(date: Date?, timeZone: String? = null): Boolean {
        if (date == null) return false
        val tz = if (timeZone != null) TimeZone.getTimeZone(timeZone) else TimeZone.getDefault()
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        sdf.timeZone = tz
        
        val now = sdf.format(Date())
        val target = sdf.format(date)
        
        val match = now == target
        return match
    }

    fun shouldShowDay(date: Date?, timeZone: String? = null): Boolean {
        return !isToday(date, timeZone)
    }
}
