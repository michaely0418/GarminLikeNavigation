package app.garminlike.nav.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Garmin-like high-contrast Speedometer & Safety Telemetry Card.
 * Displays glanceable digital speed (>= 44sp), Taiwan regulatory speed limit sign,
 * and overspeed warning.
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 2 & 4.2.
 */
@Composable
fun SpeedometerTelemetryCard(
    currentSpeedKmh: Int,
    speedLimitKmh: Int?,
    isOverspeed: Boolean,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val speedTextColor = if (isOverspeed) HudColors.CrimsonRed else HudColors.PureWhite
    val statusText = when {
        isOverspeed -> "超速警示"
        speedLimitKmh != null -> "車速正常"
        else -> "---"
    }
    val statusColor = when {
        isOverspeed -> HudColors.CrimsonRed
        speedLimitKmh != null -> HudColors.EmeraldGreen
        else -> HudColors.TextMuted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HudColors.SurfaceCard)
            .border(
                width = if (isOverspeed) 2.dp else 1.dp,
                color = if (isOverspeed) HudColors.CrimsonRed else HudColors.CardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(if (isCompact) 10.dp else 14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speed Limit Sign
            TaiwanSpeedLimitBadge(
                limitKmh = speedLimitKmh,
                isOverspeed = isOverspeed,
                size = if (isCompact) 52.dp else 60.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Digital Speed (>= 44sp)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentSpeedKmh.toString(),
                    color = speedTextColor,
                    fontSize = if (isCompact) 44.sp else 50.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "km/h",
                    color = HudColors.TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Status chip
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
