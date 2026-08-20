//
//  AnimatedWeatherBackground.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.forescoop.ventus.data.model.SpotForecast
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedWeatherBackground(
    forecast: SpotForecast,
    hour: String? = null,
    modifier: Modifier = Modifier
) {
    val condition = remember(forecast, hour) { WeatherBackgroundCondition(forecast, hour) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "weather")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Base Gradient
        val bgColors = remember(condition) {
            when {
                condition.isNight -> listOf(Color(0xFF1A237E), Color(0xFF0D47A1), Color.Transparent)
                condition.isSunny -> listOf(Color(0xFFFFEB3B).copy(alpha = 0.25f), Color(0xFF00BCD4).copy(alpha = 0.17f), Color.Transparent)
                condition.isRainy || condition.isSnowy -> listOf(Color(0xFF2196F3).copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.14f), Color.Transparent)
                else -> listOf(Color.Gray.copy(alpha = 0.15f), Color(0xFF2196F3).copy(alpha = 0.08f), Color.Transparent)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = bgColors,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )

            // Sun / Moon positioning (simplified)
            if (condition.isSunny) {
                drawCircle(
                    color = Color.Yellow.copy(alpha = 0.3f),
                    radius = 110.dp.toPx() * (0.92f + animationProgress * 0.16f),
                    center = Offset(
                        size.width * (0.2f + animationProgress * 0.08f),
                        size.height * (0.2f + (1f - animationProgress) * 0.06f)
                    )
                )
            }
        }

        // Precipitation
        if (condition.isRainy || condition.isSnowy) {
            PrecipitationOverlay(condition, infiniteTransition)
        }
    }
}

@Composable
fun PrecipitationOverlay(
    condition: WeatherBackgroundCondition,
    infiniteTransition: InfiniteTransition
) {
    val particleCount = if (condition.isSnowy) 50 else 30
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        repeat(particleCount) { index ->
            val xFactor = (index * 37 % 100) / 100f
            val speedFactor = 0.5f + (index * 13 % 100) / 100f
            val yProgress = (time * speedFactor * 0.1f + (index * 23 % 100) / 100f) % 1f
            
            val x = xFactor * width
            val y = yProgress * height
            
            if (condition.isSnowy) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 2.dp.toPx(),
                    center = Offset(x + sin(yProgress * 10f + index) * 10f, y)
                )
            } else {
                drawLine(
                    color = Color.Cyan.copy(alpha = 0.4f),
                    start = Offset(x, y),
                    end = Offset(x, y + 10.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}
