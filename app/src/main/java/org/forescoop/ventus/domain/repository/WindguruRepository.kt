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
import org.forescoop.ventus.data.model.SpotResult
import org.forescoop.ventus.data.model.User
import org.forescoop.ventus.data.remote.WindguruService

class WindguruRepository(private val service: WindguruService) {

    suspend fun getForecast(spotId: String, modelId: String? = "3"): SpotForecast {
        return service.getForecast(spotId = spotId, modelId = modelId)
    }

    suspend fun getSpotInfo(spotId: String): SpotInfo {
        return service.getSpotInfo(spotId = spotId)
    }

    suspend fun searchSpots(query: String): SpotResult {
        return service.searchSpots(search = query)
    }

    suspend fun login(username: String, password: String): User {
        return service.login(username = username, password = password)
    }

    suspend fun getWForecast(
        spotId: String,
        modelId: String? = "3",
        username: String?,
        password: String?
    ): SpotForecast {
        return service.getWForecast(
            spotId = spotId,
            modelId = modelId,
            username = username,
            password = password
        )
    }

    suspend fun getWForecastLatLon(
        latitude: Double,
        longitude: Double,
        modelId: String? = "3",
        username: String?,
        password: String?
    ): SpotForecast {
        return service.getWForecastLatLon(
            latitude = latitude,
            longitude = longitude,
            modelId = modelId,
            username = username,
            password = password
        )
    }
}
