package com.pes.androidrecipenest.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = OnTerracottaPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = SaffronSecondary,
    onSecondary = OnSaffronSecondary,
    secondaryContainer = SaffronContainer,
    onSecondaryContainer = OnSaffronContainer,
    tertiary = HerbTertiary,
    onTertiary = OnHerbTertiary,
    tertiaryContainer = HerbContainer,
    onTertiaryContainer = OnHerbTertiaryContainer,
    background = WarmCreamBackground,
    surface = WarmCreamBackground,
    surfaceVariant = WarmSurfaceVariant,
    onBackground = EspressoText,
    onSurface = EspressoText
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaDarkPrimary,
    onPrimary = OnTerracottaDarkPrimary,
    primaryContainer = TerracottaDarkContainer,
    onPrimaryContainer = OnTerracottaDarkContainer,
    secondary = SaffronDarkSecondary,
    onSecondary = OnSaffronDarkSecondary,
    secondaryContainer = SaffronDarkContainer,
    onSecondaryContainer = OnSaffronDarkContainer,
    tertiary = HerbDarkTertiary,
    onTertiary = OnHerbDarkTertiary,
    tertiaryContainer = HerbDarkContainer,
    onTertiaryContainer = OnHerbDarkTertiaryContainer,
    background = DarkBistroBackground,
    surface = DarkBistroBackground,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = CreamDarkText,
    onSurface = CreamDarkText
)

@Composable
fun AndroidRecipeNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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