package com.munireport.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.munireport.app.core.designsystem.colors.MuniBlue100
import com.munireport.app.core.designsystem.colors.MuniBlue200
import com.munireport.app.core.designsystem.colors.MuniBlue600
import com.munireport.app.core.designsystem.colors.MuniBlue900
import com.munireport.app.core.designsystem.colors.MuniBorder
import com.munireport.app.core.designsystem.colors.MuniCanvas
import com.munireport.app.core.designsystem.colors.MuniGreen100
import com.munireport.app.core.designsystem.colors.MuniGreen600
import com.munireport.app.core.designsystem.colors.MuniGreen700
import com.munireport.app.core.designsystem.colors.MuniInk
import com.munireport.app.core.designsystem.colors.MuniInkMuted
import com.munireport.app.core.designsystem.colors.MuniRed100
import com.munireport.app.core.designsystem.colors.MuniRed600
import com.munireport.app.core.designsystem.colors.MuniRed700
import com.munireport.app.core.designsystem.colors.MuniSurface
import com.munireport.app.core.designsystem.colors.MuniSurfaceMuted
import com.munireport.app.core.designsystem.colors.MuniYellow100
import com.munireport.app.core.designsystem.colors.MuniYellow500
import com.munireport.app.core.designsystem.colors.MuniYellow700
import com.munireport.app.core.designsystem.typography.MuniTypography

private val LightColors = lightColorScheme(
    primary = MuniBlue600,
    onPrimary = Color.White,
    primaryContainer = MuniBlue100,
    onPrimaryContainer = MuniBlue900,
    secondary = MuniYellow500,
    onSecondary = MuniYellow700,
    secondaryContainer = MuniYellow100,
    onSecondaryContainer = MuniYellow700,
    tertiary = MuniGreen600,
    onTertiary = Color.White,
    tertiaryContainer = MuniGreen100,
    onTertiaryContainer = MuniGreen700,
    background = MuniCanvas,
    onBackground = MuniInk,
    surface = MuniSurface,
    onSurface = MuniInk,
    surfaceVariant = MuniSurfaceMuted,
    onSurfaceVariant = MuniInkMuted,
    surfaceContainer = MuniCanvas,
    surfaceContainerHigh = MuniSurfaceMuted,
    surfaceContainerHighest = MuniBorder,
    outline = MuniBlue200,
    outlineVariant = MuniBorder,
    error = MuniRed600,
    onError = Color.White,
    errorContainer = MuniRed100,
    onErrorContainer = MuniRed700,
    inverseSurface = MuniInk,
    inverseOnSurface = MuniCanvas,
)

private val MuniShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MuniReportTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MuniTypography,
        shapes = MuniShapes,
        content = content,
    )
}
