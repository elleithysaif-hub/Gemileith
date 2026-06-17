package com.gemileith.launcher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.getValue

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
      primary = PrimaryBlue,
      onPrimary = OnPrimaryBlue,
      secondary = SecondaryGreen,
      onSecondary = OnSecondaryGreen,
      background = BackgroundDark,
      onBackground = TextPrimary,
      surface = SurfaceDark,
      onSurface = TextPrimary,
      surfaceVariant = SurfaceVariantDark,
      onSurfaceVariant = TextMuted,
      outline = BorderColor1,
      outlineVariant = BorderColor2,
  )

private val LightColorScheme =
  lightColorScheme(
      primary = Color(0xFF0061A4),
      onPrimary = Color(0xFFFFFFFF),
      secondary = Color(0xFF386A20),
      onSecondary = Color(0xFFFFFFFF),
      background = Color(0xFFFDFBFF),
      onBackground = Color(0xFF1A1C1E),
      surface = Color(0xFFFDFBFF),
      onSurface = Color(0xFF1A1C1E),
      surfaceVariant = Color(0xFFDFE2EB),
      onSurfaceVariant = Color(0xFF43474E),
      outline = Color(0xFF73777F),
      outlineVariant = Color(0xFFC3C7CF),
  )

@Composable
fun animateColorScheme(target: ColorScheme): ColorScheme {
    val primary by animateColorAsState(target.primary, tween(500), label = "primary")
    val onPrimary by animateColorAsState(target.onPrimary, tween(500), label = "")
    val secondary by animateColorAsState(target.secondary, tween(500), label = "")
    val onSecondary by animateColorAsState(target.onSecondary, tween(500), label = "")
    val background by animateColorAsState(target.background, tween(500), label = "")
    val onBackground by animateColorAsState(target.onBackground, tween(500), label = "")
    val surface by animateColorAsState(target.surface, tween(500), label = "")
    val onSurface by animateColorAsState(target.onSurface, tween(500), label = "")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, tween(500), label = "")
    val onSurfaceVariant by animateColorAsState(target.onSurfaceVariant, tween(500), label = "")
    val outline by animateColorAsState(target.outline, tween(500), label = "")
    val outlineVariant by animateColorAsState(target.outlineVariant, tween(500), label = "")
    
    return ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = target.primaryContainer,
        onPrimaryContainer = target.onPrimaryContainer,
        inversePrimary = target.inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = target.secondaryContainer,
        onSecondaryContainer = target.onSecondaryContainer,
        tertiary = target.tertiary,
        onTertiary = target.onTertiary,
        tertiaryContainer = target.tertiaryContainer,
        onTertiaryContainer = target.onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = target.surfaceTint,
        inverseSurface = target.inverseSurface,
        inverseOnSurface = target.inverseOnSurface,
        error = target.error,
        onError = target.onError,
        errorContainer = target.errorContainer,
        onErrorContainer = target.onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = target.scrim,
        surfaceBright = target.surfaceBright,
        surfaceDim = target.surfaceDim,
        surfaceContainer = target.surfaceContainer,
        surfaceContainerHigh = target.surfaceContainerHigh,
        surfaceContainerHighest = target.surfaceContainerHighest,
        surfaceContainerLow = target.surfaceContainerLow,
        surfaceContainerLowest = target.surfaceContainerLowest
    )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val baseColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val colorScheme = animateColorScheme(baseColorScheme)
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      @Suppress("DEPRECATION")
      window.statusBarColor = Color.Transparent.toArgb()
      @Suppress("DEPRECATION")
      window.navigationBarColor = Color.Transparent.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
