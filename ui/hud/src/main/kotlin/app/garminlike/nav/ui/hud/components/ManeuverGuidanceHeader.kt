package app.garminlike.nav.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.core.model.Maneuver
import app.garminlike.nav.core.model.ManeuverType
import app.garminlike.nav.ui.hud.model.HudFormatters
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * High-contrast Garmin-style Maneuver Card.
 * Displays next turn arrow, large countdown distance (>= 36sp), and clear lane/road instruction.
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 4.2.
 */
@Composable
fun ManeuverGuidanceHeader(
    maneuver: Maneuver?,
    distanceToManeuverMeters: Double?,
    laneGuidanceText: String?,
    demoInstructionText: String? = null,
    demoSubInstructionText: String? = null,
    modifier: Modifier = Modifier
) {
    val distanceDisplay = HudFormatters.formatDistance(distanceToManeuverMeters)
    val isUnrouted = maneuver == null

    val instructionTitle = when {
        !isUnrouted -> maneuver?.instruction ?: ""
        demoInstructionText != null -> demoInstructionText
        else -> "未開始導航"
    }

    val subTitle = when {
        !isUnrouted && laneGuidanceText != null -> "車道提示: $laneGuidanceText"
        demoSubInstructionText != null -> demoSubInstructionText
        else -> "等待目標航點或路徑指令"
    }

    val maneuverType = maneuver?.type ?: ManeuverType.DEPART
    val iconColor = if (maneuver?.type == ManeuverType.KEEP_LEFT || maneuver?.type == ManeuverType.KEEP_RIGHT) {
        HudColors.AzureBlue
    } else {
        HudColors.EmeraldGreen
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HudColors.SurfaceCard)
            .border(1.dp, HudColors.CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Directional Turn Icon
                ManeuverIcon(
                    maneuverType = if (isUnrouted) null else maneuverType,
                    size = 52.dp,
                    tint = if (isUnrouted) HudColors.TextMuted else iconColor
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Big Countdown Distance Readout (>= 36sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = distanceDisplay,
                        color = if (isUnrouted) HudColors.TextMuted else HudColors.PureWhite,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        maxLines = 1
                    )

                    Text(
                        text = instructionTitle,
                        color = HudColors.PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-instruction / Lane Guidance or Demo Notice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subTitle,
                    color = if (isUnrouted) HudColors.VividAmber else HudColors.AzureBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Visual Progress countdown bar (if distance is actively counting down under 1000m)
            if (!isUnrouted && distanceToManeuverMeters != null && distanceToManeuverMeters <= 1000.0) {
                Spacer(modifier = Modifier.height(6.dp))
                val progress = (1.0 - (distanceToManeuverMeters / 1000.0)).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = iconColor,
                    trackColor = HudColors.SurfaceCardElevated
                )
            }
        }
    }
}
