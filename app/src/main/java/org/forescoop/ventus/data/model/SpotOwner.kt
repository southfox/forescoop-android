//
//  SpotOwner.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.data.model

import com.google.gson.annotations.SerializedName

class SpotOwner(
    idSpot: String? = null,
    spotName: String? = null,
    country: String? = null,
    nickname: String? = null,
    @SerializedName("id_user") val idUser: String? = null
) : Spot(idSpot, spotName, country, nickname) {
    override fun toString(): String {
        return super.toString() + ", SpotOwner: $idUser"
    }
}
