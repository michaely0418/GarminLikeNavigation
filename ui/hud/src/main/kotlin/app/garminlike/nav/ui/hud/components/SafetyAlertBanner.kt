package app.garminlike.nav.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.core.model.SafetyAlert
import app.garminlike.nav.core.model.SafetyAlertKind
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Toast / Banner for safety and enforcement camera alerts along the route.
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 4.2.
 */
@Composable
fun SafetyAlertBanner(
    alert: SafetyAlert?,
    modifier: Modifier = Modifier
) {
    if (alert == null) return

    val isCritical = alert.kind == SafetyAlertKind.SPEED_CAMERA || alert.kind == SafetyAlertKind.SECTION_SPEED
    val bannerBg = if (isCritical) HudColors.CrimsonRedGlow else HudColors.VividAmberSubtle
    val accentColor = if (isCritical) HudColors.CrimsonRed else HudColors.VividAmber

    val kindBadge = when (alert.kind) {
        SafetyAlertKind.SPEED_CAMERA -> "固定測速"
        SafetyAlertKind.SECTION_SPEED -> "區間測速"
        SafetyAlertKind.RED_LIGHT -> "闖紅燈照相"
        SafetyAlertKind.TRAFFIC_ENFORCEMENT -> "科技執法"
        SafetyAlertKind.SPEED_LIMIT -> "速限變更"
        SafetyAlertKind.ROAD_RESTRICTION -> "路段管制"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bannerBg)
            .border(1.dp, accentColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warning Icon Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "! $kindBadge",
                    color = HudColors.PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Message and Source
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.message,
                    color = HudColors.PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "來源: ${alert.sourceId}",
                    color = HudColors.TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
