//
//  RetrofitClient.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.data.remote

import com.google.gson.GsonBuilder
import org.forescoop.ventus.data.model.Forecast
import org.forescoop.ventus.data.model.ForecastDeserializer
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.data.model.SpotForecastDeserializer
import org.forescoop.ventus.data.model.SpotResult
import org.forescoop.ventus.data.model.SpotResultDeserializer
import org.forescoop.ventus.data.model.TimeWeather
import org.forescoop.ventus.data.model.TimeWeatherDeserializer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    private val gson = GsonBuilder()
        .registerTypeAdapter(TimeWeather::class.java, TimeWeatherDeserializer())
        .registerTypeAdapter(Forecast::class.java, ForecastDeserializer())
        .registerTypeAdapter(SpotForecast::class.java, SpotForecastDeserializer())
        .registerTypeAdapter(SpotResult::class.java, SpotResultDeserializer())
        .create()

    val windguruService: WindguruService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WindguruService::class.java)
    }

    companion object {
        private const val BASE_URL = "https://www.windguru.cz/"
    }
}
