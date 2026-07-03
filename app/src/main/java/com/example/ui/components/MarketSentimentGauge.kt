package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MarketSentimentGauge(
    score: Int, // 0 to 100
    label: String, // e.g., "إيجابي"
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "مؤشر معنويات السوق (Gemini AI)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                val gaugeColor = when {
                    score >= 70 -> Color(0xFF10B981) // Strong Bullish
                    score >= 50 -> Color(0xFF34A853) // Bullish
                    score >= 30 -> Color(0xFFF9AB00) // Neutral/Bearish
                    else -> Color(0xFFEF4444) // Strong Bearish
                }

                Canvas(
                    modifier = Modifier
                        .size(200.dp, 100.dp)
                        .padding(top = 10.dp)
                ) {
                    val strokeWidth = 24f
                    val radius = size.width / 2 - strokeWidth
                    val center = Offset(size.width / 2, size.height)
                    val sizeArc = Size(radius * 2, radius * 2)
                    val topLeft = Offset(center.x - radius, center.y - radius)

                    // Draw background arc
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = sizeArc,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Draw progress arc
                    val sweepAngle = (score / 100f) * 180f
                    drawArc(
                        color = gaugeColor,
                        startAngle = 180f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = sizeArc,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Draw needle
                    val needleAngle = 180f + sweepAngle
                    val needleAngleRad = Math.toRadians(needleAngle.toDouble())
                    val needleLength = radius - 10f
                    val needleEndX = center.x + (cos(needleAngleRad) * needleLength).toFloat()
                    val needleEndY = center.y + (sin(needleAngleRad) * needleLength).toFloat()

                    drawLine(
                        color = Color.DarkGray,
                        start = center,
                        end = Offset(needleEndX, needleEndY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = Color.DarkGray,
                        radius = 8f,
                        center = center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = 12.dp)
                ) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = gaugeColor
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = gaugeColor
                    )
                }
            }
        }
    }
}
