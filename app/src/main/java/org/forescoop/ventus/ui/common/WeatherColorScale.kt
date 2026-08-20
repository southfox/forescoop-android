//
//  WeatherColorScale.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import androidx.compose.ui.graphics.Color

object WeatherColorScale {
    fun temperatureColor(celsius: Double): Color {
        return when {
            celsius < -10 -> Color(0xFF9ECCFF)
            celsius < 0 -> Color(0xFF599EFF)
            celsius < 10 -> Color(0xFF4CD1F2)
            celsius < 18 -> Color(0xFF66CC8C)
            celsius < 25 -> Color.Yellow
            celsius < 32 -> Color(0xFFFFA500) // Orange
            else -> Color.Red
        }
    }

    fun windColor(knots: Double): Color {
        val kmh = knots * 1.852
        val intensity = (kmh / 80.0).coerceIn(0.0, 1.0).toFloat()
        return Color(0xFF00FFFF).copy(alpha = 0.55f * intensity)
    }

    fun gustColor(knots: Double): Color {
        val kmh = knots * 1.852
        val intensity = (kmh / 100.0).coerceIn(0.0, 1.0).toFloat()
        return Color(0xFFF5FFFA).copy(alpha = 0.65f * intensity) // MintCream-ish
    }
    
    fun cloudColor(percent: Int): Color {
        val intensity = (percent / 100.0).coerceIn(0.0, 1.0).toFloat()
        return Color.Gray.copy(alpha = 0.65f * intensity)
    }

    fun precipitationColor(mm: Double): Color {
        val intensity = (mm / 5.0).coerceIn(0.0, 1.0).toFloat()
        return Color.Blue.copy(alpha = 0.5f * intensity)
    }
}
