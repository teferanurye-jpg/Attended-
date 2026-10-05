package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = EthioEmeraldBright,
        onPrimary = EthioEmeraldDark,
        primaryContainer = EthioEmerald,
        onPrimaryContainer = EthioMint,
        secondary = EthioGoldLight,
        onSecondary = Color(0xFF3E2800),
        secondaryContainer = Color(0xFF5A3C00),
        onSecondaryContainer = EthioGoldContainer,
        tertiary = Color(0xFF88C5E6),
        background = Color(0xFF111413),
        surface = Color(0xFF191C1B),
        surfaceVariant = Color(0xFF262C29),
        onSurface = Color(0xFFE1E3E0),
        onBackground = Color(0xFFE1E3E0),
        outline = Color(0xFF8A938E)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = EthioEmerald,
        onPrimary = Color.White,
        primaryContainer = EthioMint,
        onPrimaryContainer = EthioEmeraldDark,
        secondary = EthioGold,
        onSecondary = Color.White,
        secondaryContainer = EthioGoldContainer,
        onSecondaryContainer = Color(0xFF4B3400),
        tertiary = EthioSlate,
        onTertiary = Color.White,
        background = Color(0xFFF7F9F8),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE8ECE9),
        onSurface = NeutralDark,
        onBackground = NeutralDark,
        outline = Color(0xFF707974)
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our distinct Ethio brand colors for consistency
    content: @Composable () -> Unit,
) {
  val colorScheme =
      when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
      }

  MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
  )
}
