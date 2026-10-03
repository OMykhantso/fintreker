package com.example.fintreker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.fintreker.domain.BudgetStatus
import com.example.fintreker.domain.Category

private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A6360),
    background = Color(0xFFF7FAF9),
    surface = Color(0xFFF7FAF9)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF00504A),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFB1CCC8),
    background = Color(0xFF0F1413),
    surface = Color(0xFF0F1413)
)

/** Колір попередження (≥ 80% ліміту) — Material 3 не має для нього готового ролі. */
val WarningColor = Color(0xFFF9A825)

@Composable
fun FinTrekerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

/** Колір категорії береться з domain (єдине джерело правди), тут лише перетворення в Compose. */
fun Category.color(): Color = Color(colorArgb)

@Composable
fun BudgetStatus.color(): Color = when (this) {
    BudgetStatus.NO_LIMIT, BudgetStatus.OK -> MaterialTheme.colorScheme.primary
    BudgetStatus.WARNING -> WarningColor
    BudgetStatus.EXCEEDED -> MaterialTheme.colorScheme.error
}
