//
//  Spot.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.data.model

import com.google.gson.annotations.SerializedName

open class Spot(
    @SerializedName("id_spot") val idSpot: String? = null,
    @SerializedName("spotname") val spotName: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("nickname") val nickname: String? = null
) {
    val identifier: String? get() = idSpot
    val spot: String? get() = spotName
    val countryName: String? get() = country
    val nickName: String? get() = nickname

    val displayName: String
        get() {
            if (identifier.isNullOrEmpty() || identifier == "0") {
                return if (!spot.isNullOrEmpty()) spot!! else "Unknown location"
            }
            return "Spot #$identifier"
        }

    val isCoordinateLocation: Boolean get() = identifier == "0"

    override fun toString(): String {
        return listOfNotNull("Spot", idSpot, spotName, country, nickname).joinToString(", ")
    }
}
