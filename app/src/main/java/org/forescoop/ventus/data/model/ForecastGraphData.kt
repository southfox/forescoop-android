//
//  ForecastGraphData.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.data.model

import java.util.Date

data class ForecastGraphPoint(
    val id: String,
    val date: Date,
    val wind: Double,
    val gust: Double,
    val direction: Double?,
    val cloudCover: Double,
    val humidity: Double,
    val pressure: Double,
    val temperature: Double,
    val precipitation: Double,
    val wave: Double? = null
)

data class ForecastGraphSeries(
    val id: String,
    val name: String,
    val points: List<ForecastGraphPoint>
)
