package com.example.fintreker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fintreker.domain.CategoryShare
import com.example.fintreker.domain.formatPercent

/**
 * Кругова діаграма розподілу витрат за категоріями (Лаб 4, AI-завдання).
 * Кольори беруться з `Category` (єдине джерело правди); сегменти плавно «розгортаються» при появі.
 * Для TalkBack діаграма має текстовий опис усіх часток.
 */
@Composable
fun PieChart(
    shares: List<CategoryShare>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 32.dp,
    centerContent: @Composable () -> Unit = {}
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(shares) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }

    val description = remember(shares) {
        shares.joinToString(", ") { "${it.category.label} ${formatPercent(it.fraction.toDouble())}" }
    }

    Box(
        modifier = modifier.semantics { contentDescription = "Кругова діаграма витрат: $description" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            var startAngle = -90f
            shares.forEach { share ->
                val sweep = share.fraction * 360f * progress.value
                drawArc(
                    color = Color(share.category.colorArgb),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke)
                )
                startAngle += sweep
            }
        }
        centerContent()
    }
}
