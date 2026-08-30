//
//  ForecastMoonInfo.kt
//  Ventus
//

package org.forescoop.ventus.data.model

import java.util.Date
import kotlin.math.roundToInt

data class ForecastMoonInfo(
    val day: Date,
    val phaseNameResId: Int,
    val illumination: Double,
    val symbolName: String,
    val moonrise: Date?,
    val moonset: Date?
) {
    val illuminationText: String get() = "${(illumination * 100).roundToInt()}%"
}
