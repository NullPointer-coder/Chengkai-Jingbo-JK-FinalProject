package com.example.smartreciperecommenderapp.ui.homeScreen.units

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

@Composable
fun MacrosDonutChart(
    calories: Int,
    fat: Double?,
    carbs: Double?,
    protein: Double?,
    modifier: Modifier = Modifier
) {
    // This composable draws a donut chart representing the ratio of fat, carbs, and protein.
    // In the center, it shows the calorie count.
    // On the right, it shows the percentage and grams of each macro.

    val fatVal = fat ?: 0.0
    val carbsVal = carbs ?: 0.0
    val proteinVal = protein ?: 0.0

    // Calculate the total and ratios
    val total = fatVal + carbsVal + proteinVal
    val fatRatio = if (total > 0) fatVal / total else 0.0
    val carbsRatio = if (total > 0) carbsVal / total else 0.0
    val proteinRatio = if (total > 0) proteinVal / total else 0.0

    // Colors for each macro segment
    val fatColor = Color(0xFFFF6F61)       // Red
    val carbsColor = Color(0xFF4FB3FF)     // Blue
    val proteinColor = Color(0xFFFFD700)   // Gold

    val chartSize = 150.dp
    val strokeWidth = 20f

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut chart area
        Box(
            modifier = Modifier
                .size(chartSize)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.minDimension

                // Start angle at -90 degrees so that we begin drawing from top
                var startAngle = -90f

                // Draw fat arc
                val fatSweep = (fatRatio * 360f).toFloat()
                drawArc(
                    color = fatColor,
                    startAngle = startAngle,
                    sweepAngle = fatSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(0f, 0f),
                    size = Size(diameter, diameter)
                )
                startAngle += fatSweep

                // Draw carbs arc
                val carbsSweep = (carbsRatio * 360f).toFloat()
                drawArc(
                    color = carbsColor,
                    startAngle = startAngle,
                    sweepAngle = carbsSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(0f, 0f),
                    size = Size(diameter, diameter)
                )
                startAngle += carbsSweep

                // Draw protein arc
                val proteinSweep = (proteinRatio * 360f).toFloat()
                drawArc(
                    color = proteinColor,
                    startAngle = startAngle,
                    sweepAngle = proteinSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(0f, 0f),
                    size = Size(diameter, diameter)
                )
            }

            // Display calories in the center of the donut
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = calories.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Macro details on the right side with percentages and grams
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fat info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = fatColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(fatRatio * 100).toInt()}% fat: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${fatVal}g",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Carbs info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = carbsColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(carbsRatio * 100).toInt()}% carbs: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${carbsVal}g",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Protein info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = proteinColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(proteinRatio * 100).toInt()}% protein: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${proteinVal}g",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}