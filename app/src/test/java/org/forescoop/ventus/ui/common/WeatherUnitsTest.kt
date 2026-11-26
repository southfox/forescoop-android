package org.forescoop.ventus.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherUnitsTest {
    @Test
    fun windSpeedConversionsAndBeaufortBoundaries() {
        assertEquals(18.52, Knots(10.0).kmh, 0.0001)
        assertEquals(5.14444, Knots(10.0).mps, 0.00001)
        assertEquals(0, Knots(0.9).bft)
        assertEquals(1, Knots(1.0).bft)
        assertEquals(11, Knots(63.9).bft)
        assertEquals(12, Knots(64.0).bft)
    }

    @Test
    fun temperatureAndPrecipitationConversions() {
        assertEquals(32.0, Temperature(0.0).value(TemperatureUnit.FAHRENHEIT), 0.0001)
        assertEquals(212.0, Temperature(100.0).value(TemperatureUnit.FAHRENHEIT), 0.0001)
        assertEquals(1.0, Precipitation(25.4).value(PrecipitationUnit.INCHES), 0.0001)
    }

    @Test
    fun preferencesHandleAliasesAndUnknownValues() {
        assertEquals(WindSpeedUnit.KNOTS, WindSpeedUnit.fromWindguruPreference(" KTS "))
        assertEquals(TemperatureUnit.FAHRENHEIT, TemperatureUnit.fromWindguruPreference("°F"))
        assertNull(WindSpeedUnit.fromWindguruPreference("unknown"))
        assertNull(TemperatureUnit.fromWindguruPreference(null))
    }
}
