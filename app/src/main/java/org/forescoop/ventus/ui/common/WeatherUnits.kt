//
//  WeatherUnits.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.ui.common

enum class WindSpeedUnit(val label: String) {
    KNOTS("knots"),
    METERS_PER_SECOND("m/s"),
    KILOMETERS_PER_HOUR("km/h"),
    MILES_PER_HOUR("mph"),
    BEAUFORT("Bft");

    companion object {
        fun fromWindguruPreference(pref: String?): WindSpeedUnit? {
            return when (pref?.trim()?.lowercase()) {
                "knots", "knot", "kt", "kts" -> KNOTS
                "m/s", "ms", "mps", "meters_per_second" -> METERS_PER_SECOND
                "km/h", "kmh", "kph", "kilometers_per_hour" -> KILOMETERS_PER_HOUR
                "mph", "miles_per_hour" -> MILES_PER_HOUR
                "bft", "beaufort" -> BEAUFORT
                else -> null
            }
        }
    }
}

enum class TemperatureUnit(val label: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    companion object {
        fun fromWindguruPreference(pref: String?): TemperatureUnit? {
            return when (pref?.trim()?.lowercase()) {
                "c", "°c", "celsius" -> CELSIUS
                "f", "°f", "fahrenheit" -> FAHRENHEIT
                else -> null
            }
        }
    }
}

class Knots(val value: Double) {
    val kmh: Double get() = value * 1.852
    val mph: Double get() = value * 1.15078
    val mps: Double get() = value * 0.514444
    
    val bft: Int
        get() = when {
            value < 1 -> 0
            value < 4 -> 1
            value < 7 -> 2
            value < 11 -> 3
            value < 16 -> 4
            value < 22 -> 5
            value < 28 -> 6
            value < 34 -> 7
            value < 41 -> 8
            value < 48 -> 9
            value < 56 -> 10
            value < 64 -> 11
            else -> 12
        }

    fun value(unit: WindSpeedUnit): Double {
        return when (unit) {
            WindSpeedUnit.KNOTS -> value
            WindSpeedUnit.METERS_PER_SECOND -> mps
            WindSpeedUnit.KILOMETERS_PER_HOUR -> kmh
            WindSpeedUnit.MILES_PER_HOUR -> mph
            WindSpeedUnit.BEAUFORT -> bft.toDouble()
        }
    }
}

data class Temperature(val celsius: Double) {
    fun value(unit: TemperatureUnit): Double {
        return when (unit) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> celsius * 9 / 5 + 32
        }
    }
}
