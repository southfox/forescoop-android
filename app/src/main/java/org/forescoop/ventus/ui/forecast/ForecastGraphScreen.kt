//
//  ForecastGraphScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forescoop.ventus.data.model.ForecastGraphPoint
import org.forescoop.ventus.data.model.SpotForecast
import org.forescoop.ventus.ui.common.Knots
import org.forescoop.ventus.ui.common.Temperature
import org.forescoop.ventus.ui.common.WindSpeedUnit
import java.text.SimpleDateFormat
import java.util.*

import org.forescoop.ventus.ui.common.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ForecastGraphScreen(
    spotForecast: SpotForecast,
    onNavigateToDashboard: () -> Unit,
    onNavigateToGrid: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean,
    isAuthenticated: Boolean,
    onNavigateBack: () -> Unit
) {
    val points = remember(spotForecast) { createPoints(spotForecast) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VentusTopBar(
                currentRoute = "graph",
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToGrid = onNavigateToGrid,
                onNavigateToGraph = onNavigateToGraph,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToProfile = onNavigateToProfile,
                onLogout = onLogout,
                onToggleFavorite = onToggleFavorite,
                isFavorite = isFavorite,
                isAuthenticated = isAuthenticated
            )
            
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = spotForecast.displayName,
                    style = MaterialTheme.typography.titleLarge
                )
                if (!spotForecast.spot.isNullOrEmpty()) {
                    Text(
                        text = spotForecast.spot!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (points.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Graph unavailable")
                }
            } else {
                ForecastGraphContent(points)
            }
        }
    }
}

@Composable
fun ForecastGraphContent(points: List<ForecastGraphPoint>) {
    val textMeasurer = rememberTextMeasurer()
    val scrollState = rememberScrollState()
    
    val columnWidth = 62.dp
    val graphWidth = columnWidth * points.size
    val canvasHeight = 630.dp // Increased to accommodate all metrics and labels
    
    Column(modifier = Modifier.fillMaxSize()) {
        ForecastGraphLegend()
        
        Box(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .width(graphWidth)
                    .height(canvasHeight)
            ) {
                drawForecastGraph(points, textMeasurer)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ForecastGraphLegend() {
    // Simplified legend for now
    FlowRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LegendItem("Wind", Color.Black)
        LegendItem("Gusts", Color.Magenta)
        LegendItem("Cloud", Color.Gray)
        LegendItem("Humidity", Color.Green)
        LegendItem("Pressure", Color.Blue)
        LegendItem("Temp", Color.Yellow)
        LegendItem("Rain", Color.Cyan)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp)
    }
}

private fun DrawScope.drawForecastGraph(points: List<ForecastGraphPoint>, textMeasurer: TextMeasurer) {
    val marginStart = 42.dp.toPx()
    val graphAreaWidth = size.width - marginStart - 16.dp.toPx()
    
    val topPadding = 40.dp.toPx()
    val windHeight = 200.dp.toPx()
    val weatherTop = topPadding + windHeight + 60.dp.toPx()
    val weatherHeight = 160.dp.toPx()
    
    val maxGust = points.maxOfOrNull { it.gust }?.coerceAtLeast(1.0) ?: 1.0
    
    val xFunc: (Int) -> Float = { index -> 
        marginStart + (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * graphAreaWidth 
    }
    val windYFunc: (Double) -> Float = { value -> 
        topPadding + windHeight - (value.toFloat() / maxGust.toFloat()) * windHeight 
    }
    val weatherYFunc: (Double) -> Float = { value -> 
        weatherTop + weatherHeight - (value.coerceIn(0.0, 100.0).toFloat() / 100f) * weatherHeight 
    }

    // Grid lines and date labels
    points.forEachIndexed { index, point ->
        if (index % 2 == 0) {
            val x = xFunc(index)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(x, topPadding),
                end = Offset(x, weatherTop + weatherHeight),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            
            val dateLabel = SimpleDateFormat("EEE d, HH:mm", Locale.getDefault()).format(point.date)
            drawText(
                textMeasurer = textMeasurer,
                text = dateLabel,
                topLeft = Offset(x - 20.dp.toPx(), topPadding - 30.dp.toPx()),
                style = TextStyle(fontSize = 9.sp, color = Color.Gray),
                size = Size(100.dp.toPx(), 20.dp.toPx())
            )
        }
    }

    // Helper for smooth path
    fun createSmoothPath(values: List<Double>, yFunc: (Double) -> Float): Path {
        val path = Path()
        if (values.isEmpty()) return path
        path.moveTo(xFunc(0), yFunc(values[0]))
        if (values.size == 1) return path
        
        for (i in 0 until values.size - 1) {
            val p0 = Offset(xFunc(maxOf(i - 1, 0)), yFunc(values[maxOf(i - 1, 0)]))
            val p1 = Offset(xFunc(i), yFunc(values[i]))
            val p2 = Offset(xFunc(i + 1), yFunc(values[i + 1]))
            val p3 = Offset(xFunc(minOf(i + 2, values.size - 1)), yFunc(values[minOf(i + 2, values.size - 1)]))
            
            val cp1 = Offset(p1.x + (p2.x - p0.x) / 6, p1.y + (p2.y - p0.y) / 6)
            val cp2 = Offset(p2.x - (p3.x - p1.x) / 6, p2.y - (p3.y - p1.y) / 6)
            
            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
        }
        return path
    }

    // Gust area
    val gustValues = points.map { it.gust }
    val gustPath = createSmoothPath(gustValues, windYFunc)
    gustPath.lineTo(xFunc(points.size - 1), topPadding + windHeight)
    gustPath.lineTo(xFunc(0), topPadding + windHeight)
    gustPath.close()
    
    drawPath(
        path = gustPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Magenta.copy(alpha = 0.6f),
                Color(0xFFFFA500).copy(alpha = 0.4f), // Orange
                Color.Cyan.copy(alpha = 0.1f)
            ),
            startY = topPadding,
            endY = topPadding + windHeight
        )
    )

    // Wind line
    val windValues = points.map { it.wind }
    val windPath = createSmoothPath(windValues, windYFunc)
    drawPath(path = windPath, color = Color.DarkGray, style = Stroke(width = 2.dp.toPx()))

    // Cloud cover area
    val cloudValues = points.map { it.cloudCover }
    val cloudPath = createSmoothPath(cloudValues, weatherYFunc)
    cloudPath.lineTo(xFunc(points.size - 1), weatherTop + weatherHeight)
    cloudPath.lineTo(xFunc(0), weatherTop + weatherHeight)
    cloudPath.close()
    drawPath(path = cloudPath, color = Color.Gray.copy(alpha = 0.3f))

    // Humidity line
    val humValues = points.map { it.humidity }
    val humPath = createSmoothPath(humValues, weatherYFunc)
    drawPath(path = humPath, color = Color(0xFF4CAF50), style = Stroke(width = 1.5.dp.toPx()))

    // Metrics and Icons
    points.forEachIndexed { index, point ->
        val x = xFunc(index)
        
        // Rain bars
        val rainHeight = (point.precipitation * 20).toFloat().coerceAtMost(28.dp.toPx())
        if (rainHeight > 0) {
            drawRect(
                color = Color.Cyan,
                topLeft = Offset(x - 2.dp.toPx(), weatherTop + weatherHeight - rainHeight),
                size = Size(4.dp.toPx(), rainHeight)
            )
        }
        
        // Temperature dots
        val tempY = weatherTop + weatherHeight - 20.dp.toPx()
        drawCircle(
            color = Color.Yellow,
            radius = 10.dp.toPx(),
            center = Offset(x, tempY)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = String.format(Locale.getDefault(), "%.0f", point.temperature),
            topLeft = Offset(x - 6.dp.toPx(), tempY - 7.dp.toPx()),
            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            size = Size(20.dp.toPx(), 20.dp.toPx())
        )
        
        // Wind direction arrows
        point.direction?.let { dir ->
            val arrowY = topPadding + windHeight + 25.dp.toPx()
            withTransform({
                translate(x, arrowY)
                rotate(dir.toFloat())
            }) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "↓",
                    topLeft = Offset(-5.dp.toPx(), -8.dp.toPx()),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    size = Size(20.dp.toPx(), 20.dp.toPx())
                )
            }
        }
        
        // Wind speed values
        drawText(
            textMeasurer = textMeasurer,
            text = String.format(Locale.getDefault(), "%.0f", point.wind),
            topLeft = Offset(x - 6.dp.toPx(), windYFunc(point.wind) - 18.dp.toPx()),
            style = TextStyle(fontSize = 8.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium),
            size = Size(25.dp.toPx(), 15.dp.toPx())
        )
    }
}

private fun createPoints(source: SpotForecast): List<ForecastGraphPoint> {
    val weather = source.forecast ?: return emptyList()
    return source.availableForecastHours.mapNotNull { hour ->
        val offset = hour.toIntOrNull() ?: return@mapNotNull null
        val date = source.forecastDate(hour) ?: return@mapNotNull null
        
        val windKts = weather.windSpeed(hour) ?: 0.0
        val gustKts = weather.windGustsKnots(hour) ?: windKts
        
        ForecastGraphPoint(
            id = hour,
            date = date,
            wind = windKts, // Simplified: using knots directly for now
            gust = gustKts,
            direction = weather.windDirection(hour),
            cloudCover = weather.cloudCoverTotal(hour)?.toDouble() ?: 0.0,
            humidity = weather.relativeHumidity(hour)?.toDouble() ?: 0.0,
            pressure = weather.seaLevelPressure(hour) ?: 0.0,
            temperature = weather.temperatureReal(hour) ?: weather.temperature(hour) ?: 0.0,
            precipitation = (weather.precipitation1(hour) ?: weather.precipitation(hour)) ?: 0.0
        )
    }
}
