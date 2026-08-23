//
//  RetrofitClient.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.data.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.forescoop.ventus.data.model.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    private val gson = GsonBuilder()
        .registerTypeAdapter(TimeWeather::class.java, TimeWeatherDeserializer())
        .registerTypeAdapter(Forecast::class.java, ForecastDeserializer())
        .registerTypeAdapter(SpotForecast::class.java, SpotForecastDeserializer())
        .registerTypeAdapter(SpotResult::class.java, SpotResultDeserializer())
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val windguruService: WindguruService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WindguruService::class.java)
    }

    companion object {
        private const val BASE_URL = "https://www.windguru.cz/"
    }
}
