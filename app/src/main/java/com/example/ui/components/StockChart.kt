package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun RsiMacdChart(
    rsiValue: Double,
    modifier: Modifier = Modifier
) {
    // Generate some mock historical data based on the current RSI value
    val dataPoints = generateMockDataPoints(rsiValue)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = "الرسم البياني التاريخي (RSI & Momentum)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val lineColor = MaterialTheme.colorScheme.primary
            val overboughtColor = Color(0xFFEF4444)
            val oversoldColor = Color(0xFF10B981)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val maxValue = 100f
                val minValue = 0f
                val range = maxValue - minValue

                // Draw Overbought Line (70)
                val overboughtY = height - ((70f - minValue) / range * height)
                drawLine(
                    color = overboughtColor.copy(alpha = 0.5f),
                    start = androidx.compose.ui.geometry.Offset(0f, overboughtY),
                    end = androidx.compose.ui.geometry.Offset(width, overboughtY),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw Oversold Line (30)
                val oversoldY = height - ((30f - minValue) / range * height)
                drawLine(
                    color = oversoldColor.copy(alpha = 0.5f),
                    start = androidx.compose.ui.geometry.Offset(0f, oversoldY),
                    end = androidx.compose.ui.geometry.Offset(width, oversoldY),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw Data Path
                if (dataPoints.isNotEmpty()) {
                    val path = Path()
                    val stepX = width / max(1, dataPoints.size - 1)

                    dataPoints.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = height - ((value - minValue) / range * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevY = height - ((dataPoints[index - 1] - minValue) / range * height)
                            
                            // Simple bezier curve for smooth lines
                            val controlX1 = prevX + (x - prevX) / 2
                            val controlY1 = prevY
                            val controlX2 = prevX + (x - prevX) / 2
                            val controlY2 = y

                            path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 6f)
                    )
                }
            }

            // Labels
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("100", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("70", fontSize = 10.sp, color = overboughtColor)
                Text("30", fontSize = 10.sp, color = oversoldColor)
                Text("0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("منذ 30 يوم", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("اليوم", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

private fun generateMockDataPoints(currentValue: Double): List<Float> {
    val points = mutableListOf<Float>()
    // Start with a random walk ending at currentValue
    var tempValue = currentValue.toFloat()
    points.add(tempValue)
    
    // Generate backwards
    for (i in 0 until 14) {
        val change = (Math.random() * 20 - 10).toFloat() // Random change between -10 and +10
        tempValue = min(100f, max(0f, tempValue + change))
        points.add(tempValue)
    }
    
    return points.reversed()
}
