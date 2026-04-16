package com.kingsmetric.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val KingsMetricColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF005F77),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDECF3),
    onPrimaryContainer = Color(0xFF002A33),
    secondary = Color(0xFF5A6872),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE8ED),
    onSecondaryContainer = Color(0xFF152027),
    tertiary = Color(0xFF8A5A44),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F9FB),
    onBackground = Color(0xFF172025),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172025),
    surfaceVariant = Color(0xFFE7EFF2),
    onSurfaceVariant = Color(0xFF405058),
    outline = Color(0xFFB8C7CE)
)

private val KingsMetricTypography = Typography(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

private val KingsMetricShapes = Shapes()

@Composable
fun KingsMetricTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KingsMetricColorScheme,
        typography = KingsMetricTypography,
        shapes = KingsMetricShapes,
        content = content
    )
}
