package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = Navy800,
  onPrimary = Color.White,
  primaryContainer = Navy700,
  onPrimaryContainer = Color.White,
  secondary = Gold500,
  onSecondary = Navy900,
  secondaryContainer = Gold100,
  onSecondaryContainer = Navy900,
  tertiary = StatusBlue,
  onTertiary = Color.White,
  background = AppBackground,
  onBackground = TextPrimary,
  surface = CardBackground,
  onSurface = TextPrimary,
  surfaceVariant = Navy50,
  onSurfaceVariant = TextSecondary,
  outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
  primary = Gold500,
  onPrimary = Navy900,
  primaryContainer = Navy800,
  onPrimaryContainer = Color.White,
  secondary = Gold500,
  onSecondary = Navy900,
  background = Color(0xFF0F172A),
  onBackground = Color(0xFFF8FAFC),
  surface = Color(0xFF1E293B),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
