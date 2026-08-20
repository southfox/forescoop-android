//
//  SpotInfo.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.data.model

import com.google.gson.annotations.SerializedName

open class SpotInfo(
    idSpot: String? = null,
    spotName: String? = null,
    country: String? = null,
    nickname: String? = null,
    @SerializedName("id_country") val idCountry: Int = 0,
    @SerializedName("lat") val latitude: Double = 0.0,
    @SerializedName("lon") val longitude: Double = 0.0,
    @SerializedName("alt") val altitude: Int = 0,
    @SerializedName("gmt_hour_offset") val gmtHourOffset: Int = 0,
    @SerializedName("sunrise") val sunrise: String? = null,
    @SerializedName("sunset") val sunset: String? = null,
    @SerializedName("models") val models: List<Int> = emptyList(),
    @SerializedName("tides") val tides: String? = null,
    @SerializedName("tz") val tz: String? = null
) : Spot(idSpot, spotName, country, nickname) {

    val coordinateText: String
        get() = "%.4f, %.4f".format(latitude, longitude)

    val coordinateSummary: String
        get() = "$coordinateText · $altitude m"

    override fun toString(): String {
        return super.toString() + "\nSpotInfo\n" + listOfNotNull(
            "id_country # $idCountry",
            "latitude $latitude",
            "longitude $longitude",
            "altitude # $altitude",
            tz,
            "gmtHourOffset: $gmtHourOffset",
            sunrise,
            sunset,
            tides,
            models.toString()
        ).joinToString(", ")
    }
}
