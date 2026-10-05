package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FuelRefillWithStats
import java.util.Locale

@Composable
fun ConsumptionTrendCard(
    refills: List<FuelRefillWithStats>,
    overallAverageKmL: Double,
    modifier: Modifier = Modifier
) {
    // Filter refills that have valid calculated km/L
    val validPoints = refills
        .filter { it.kmPerLiter != null && it.kmPerLiter > 0 }
        .take(8)
        .reversed() // oldest to newest for chronological left-to-right chart

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("consumption_trend_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Tendência de Consumo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Histórico de Consumo (km/L)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Evolução da autonomia por tanque cheio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (overallAverageKmL > 0) {
                    Text(
                        text = "Média: ${String.format(Locale("pt", "BR"), "%.1f", overallAverageKmL)} km/L",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (validPoints.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Abasteça com tanque cheio pelo menos 2 vezes\npara gerar a curva de consumo km/L.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outlineVariant

                val values = validPoints.map { it.kmPerLiter!!.toFloat() }
                val minVal = (values.minOrNull() ?: 10f) * 0.85f
                val maxVal = (values.maxOrNull() ?: 15f) * 1.15f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val spacing = w / (values.size - 1)

                    // Draw reference grid lines
                    val linePath = Path()
                    val fillPath = Path()

                    val points = values.mapIndexed { index, v ->
                        val x = index * spacing
                        val normalizedY = (v - minVal) / range
                        val y = h - (normalizedY * h)
                        Offset(x, y)
                    }

                    // Background dashed baseline
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Construct smooth curve
                    linePath.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, h)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }

                    fillPath.lineTo(points.last().x, h)
                    fillPath.close()

                    // Draw gradient under the curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.0f)
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw main line
                    drawPath(
                        path = linePath,
                        color = primaryColor,
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // Draw circles at data points
                    points.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 5.5f,
                            center = pt
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 3.5f,
                            center = pt
                        )
                    }
                }

                // Bottom Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    validPoints.forEach { item ->
                        Text(
                            text = "${String.format(Locale("pt", "BR"), "%.1f", item.kmPerLiter)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
