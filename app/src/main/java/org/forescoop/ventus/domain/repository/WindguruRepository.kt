//
//  WindguruRepository.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.domain.repository

import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.data.model.SpotInfo
import org.forescoop.ventus.data.remote.RetrofitClient

class WindguruRepository {
    private val service = RetrofitClient.windguruService

    suspend fun getForecast(spotId: String, modelId: String? = "3"): SpotForecast {
        return service.getForecast(spotId = spotId, modelId = modelId)
    }

    suspend fun getSpotInfo(spotId: String): SpotInfo {
        return service.getSpotInfo(spotId = spotId)
    }
}
