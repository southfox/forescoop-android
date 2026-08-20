//
//  ForecastModel.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.data.model

import com.google.gson.annotations.SerializedName

class ForecastModel(
    @SerializedName("model") val model: String? = null,
    @SerializedName("info") val info: Forecast? = null
) {
    override fun toString(): String {
        return "ForecastModel\n$model\n${info?.toString()}"
    }
}
