package app.garminlike.nav.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.ui.hud.model.HudFormatters
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Trip Summary Bar at the bottom of the HUD.
 * Displays remaining distance, ETA, duration, vehicle type badge, and safety-compliant
 * touch targets (minimum 56dp x 56dp).
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 4.2 & Section 5.
 */
@Composable
fun TripSummaryBar(
    remainingDistanceKm: Double,
    etaFormatted: String,
    remainingDurationMinutes: Long,
    vehicleType: VehicleType,
    isOfflineMode: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onCycleVehicleType: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationText = HudFormatters.formatDuration(remainingDurationMinutes)
    val distanceText = if (remainingDistanceKm > 0.0) "${remainingDistanceKm} km" else "--"
    val vehicleBadge = HudFormatters.formatVehicleTag(vehicleType)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(HudColors.SurfaceCard)
            .border(1.dp, HudColors.CardBorder, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Trip Metrics (Distance, ETA, Time)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vehicle Mode Button (Touch target >= 56dp width/height)
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HudColors.SurfaceCardElevated)
                        .border(1.dp, HudColors.AzureBlueSubtle, RoundedCornerShape(8.dp))
                        .clickable(onClick = onCycleVehicleType),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = vehicleBadge,
                        color = HudColors.PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Stats Column
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "剩餘: $distanceText",
                            color = HudColors.PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "抵達: $etaFormatted",
                            color = HudColors.EmeraldGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "時間: $durationText",
                            color = HudColors.TextMuted,
                            fontSize = 12.sp
                        )
                        if (isOfflineMode) {
                            Text(
                                text = "⚡ 離線模式",
                                color = HudColors.VividAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Right: Interactive Safety Buttons (>= 56dp x 56dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Mute Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMuted) HudColors.CrimsonRedGlow else HudColors.SurfaceCardElevated)
                        .border(
                            1.dp,
                            if (isMuted) HudColors.CrimsonRed else HudColors.CardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(onClick = onToggleMute),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isMuted) "🔇" else "🔊",
                        fontSize = 20.sp
                    )
                }

                // Reset / Recenter Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HudColors.SurfaceCardElevated)
                        .border(1.dp, HudColors.CardBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onResetClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔄",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
