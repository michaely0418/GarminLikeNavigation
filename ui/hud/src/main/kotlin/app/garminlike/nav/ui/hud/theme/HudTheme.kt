package app.garminlike.nav.ui.hud.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Garmin-like high-contrast dark color palette designed for high glanceability
 * under direct sunlight (Taiwan automotive & scooter mounts).
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 2.
 */
object HudColors {
    val BackgroundBlack = Color(0xFF121212)
    val SurfaceCard = Color(0xFF1E1E1E)
    val SurfaceCardElevated = Color(0xFF282828)
    val CardBorder = Color(0xFF383838)

    // Maneuver and Primary Accents
    val EmeraldGreen = Color(0xFF00C853)
    val EmeraldGreenSubtle = Color(0x3300C853)
    val AzureBlue = Color(0xFF00B0FF)
    val AzureBlueSubtle = Color(0x3300B0FF)

    // Safety and Warning Accents
    val CrimsonRed = Color(0xFFD50000)
    val CrimsonRedGlow = Color(0x44D50000)
    val VividAmber = Color(0xFFFF6D00)
    val VividAmberSubtle = Color(0x33FF6D00)

    // Text & Indicators
    val PureWhite = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFFB0B0B0)
    val TextDisabled = Color(0xFF6E6E6E)
    val TaiwanSpeedSignBorder = Color(0xFFD50000)
    val TaiwanSpeedSignBg = Color(0xFFFFFFFF)
    val TaiwanSpeedSignText = Color(0xFF000000)

    // Vehicle Badge Colors
    val CarAccent = Color(0xFF448AFF)
    val ScooterAccent = Color(0xFF00E676)
    val HeavyMotorcycleAccent = Color(0xFFFFD600)
}

val HudDarkColorScheme: ColorScheme = darkColorScheme(
    primary = HudColors.EmeraldGreen,
    onPrimary = Color.Black,
    primaryContainer = HudColors.EmeraldGreenSubtle,
    onPrimaryContainer = HudColors.PureWhite,
    secondary = HudColors.AzureBlue,
    onSecondary = Color.Black,
    surface = HudColors.SurfaceCard,
    onSurface = HudColors.PureWhite,
    background = HudColors.BackgroundBlack,
    onBackground = HudColors.PureWhite,
    error = HudColors.CrimsonRed,
    onError = HudColors.PureWhite
)

@Composable
fun GarminHudTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HudDarkColorScheme,
        content = content
    )
}
