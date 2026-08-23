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
    fun formatHour(context: Context, date: Date?): String {
        if (date == null) return "--"
        val is24Hour = DateFormat.is24HourFormat(context)
        return if (is24Hour) {
            SimpleDateFormat("H", Locale.getDefault()).format(date) + "h"
        } else {
            // Using "h a" results in "1 PM". User said "am/pm", let's use "h a" but lowercase or uppercase? 
            // Most Android systems use uppercase "AM/PM" with "a".
            SimpleDateFormat("h a", Locale.getDefault()).format(date)
        }
    }

    fun formatDay(date: Date?): String {
        if (date == null) return ""
        return SimpleDateFormat("EEE d", Locale.getDefault()).format(date)
    }

    fun isToday(date: Date?): Boolean {
        if (date == null) return false
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun shouldShowDay(date: Date?): Boolean {
        return !isToday(date)
    }
}
