package com.ozcomingfroo.mybudget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ozcomingfroo.mybudget.data.preferences.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = BudgetWarmYellow,
    onPrimary = Color.Black,
    primaryContainer = BudgetWarmYellowContainer,
    onPrimaryContainer = Color(0xFFFFE8A8),
    secondary = BudgetMint,
    tertiary = WarningAmber,
    background = BudgetBlack,
    onBackground = Color(0xFFE7EEE8),
    surface = BudgetDarkSurface,
    onSurface = Color(0xFFE7EEE8),
    surfaceVariant = Color(0xFF26312A),
    onSurfaceVariant = Color(0xFFC2CEC5),
    error = Color(0xFFFFB4AB),
)

private val LightColorScheme = lightColorScheme(
    primary = BudgetGreen,
    onPrimary = Color.White,
    primaryContainer = BudgetGreenContainer,
    onPrimaryContainer = BudgetGreenDark,
    secondary = BudgetMint,
    tertiary = WarningAmber,
    background = BudgetSurface,
    onBackground = Color(0xFF17201A),
    surface = Color.White,
    onSurface = Color(0xFF17201A),
    surfaceVariant = Color(0xFFE7EFE7),
    onSurfaceVariant = Color(0xFF4C5B50),
    error = ExpenseRed,
)

@Composable
fun MyBudgetTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.DEFAULT -> false
        AppThemeMode.NIGHT -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
