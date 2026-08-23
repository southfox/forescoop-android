//
//  WindguruService.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.data.remote

import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.data.model.SpotInfo
import org.forescoop.ventus.data.model.SpotResult
import org.forescoop.ventus.data.model.User
import retrofit2.http.GET
import retrofit2.http.Query

interface WindguruService {

    @GET("int/jsonapi.php?client=wgapp")
    suspend fun getForecast(
        @Query("q") query: String = "forecast",
        @Query("id_spot") spotId: String,
        @Query("id_model") modelId: String? = "3"
    ): SpotForecast

    @GET("int/jsonapi.php?client=wgapp")
    suspend fun getSpotInfo(
        @Query("q") query: String = "spot",
        @Query("id_spot") spotId: String
    ): SpotInfo

    @GET("int/jsonapi.php?client=wgapp")
    suspend fun searchSpots(
        @Query("q") query: String = "search_spots",
        @Query("search") search: String
    ): SpotResult

    @GET("int/jsonapi.php?client=wgapp")
    suspend fun login(
        @Query("q") query: String = "user",
        @Query("username") username: String?,
        @Query("password") password: String?
    ): User

    @GET("int/jsonapi.php?client=wgapp")
    suspend fun getWForecast(
        @Query("q") query: String = "wforecast",
        @Query("id_spot") spotId: String,
        @Query("id_model") modelId: String? = "3",
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("no_wave") noWave: Int? = null
    ): SpotForecast

    @GET("int/jsonapi.php?client=wgapp")
    suspend fun getWForecastLatLon(
        @Query("q") query: String = "wforecast_latlon",
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("id_model") modelId: String? = "3",
        @Query("username") username: String?,
        @Query("password") password: String?,
        @Query("no_wave") noWave: Int? = null
    ): SpotForecast
}
