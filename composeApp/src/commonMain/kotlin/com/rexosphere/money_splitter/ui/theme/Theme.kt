package com.rexosphere.money_splitter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

// Custom colors that aren't part of Material 3 color scheme
data class CustomColors(
    val positiveAmount: androidx.compose.ui.graphics.Color,
    val negativeAmount: androidx.compose.ui.graphics.Color,
    val neutralAmount: androidx.compose.ui.graphics.Color,
    val gradientStart: androidx.compose.ui.graphics.Color,
    val gradientEnd: androidx.compose.ui.graphics.Color
)

val LocalCustomColors = compositionLocalOf {
    CustomColors(
        positiveAmount = PositiveAmount,
        negativeAmount = NegativeAmount,
        neutralAmount = NeutralAmount,
        gradientStart = GradientStart,
        gradientEnd = GradientEnd
    )
}

@Composable
fun MoneySplitterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val customColors = if (darkTheme) {
        CustomColors(
            positiveAmount = PositiveAmountDark,
            negativeAmount = NegativeAmountDark,
            neutralAmount = NeutralAmountDark,
            gradientStart = GradientStartDark,
            gradientEnd = GradientEndDark
        )
    } else {
        CustomColors(
            positiveAmount = PositiveAmount,
            negativeAmount = NegativeAmount,
            neutralAmount = NeutralAmount,
            gradientStart = GradientStart,
            gradientEnd = GradientEnd
        )
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

// Extension property to access custom colors
val MaterialTheme.customColors: CustomColors
    @Composable
    get() = LocalCustomColors.current
