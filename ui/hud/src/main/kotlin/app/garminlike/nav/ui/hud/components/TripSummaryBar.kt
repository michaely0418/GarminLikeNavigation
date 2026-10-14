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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.ui.hud.model.HudFormatters
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Trip Summary Bar for Landscape mode.
 * Spans full width at bottom of screen with >= 56dp touch targets.
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 3.1 & 4.2.
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

/**
 * Dedicated Trip Stats Card for Portrait mode.
 * Solves H3 layout cramping on 320dp–390dp screens and large font scaling.
 *
 * Spec reference: docs/HUD_WIREFRAME.md Section 3.2.
 */
@Composable
fun PortraitTripStatsCard(
    remainingDistanceKm: Double,
    etaFormatted: String,
    remainingDurationMinutes: Long,
    isOfflineMode: Boolean,
    modifier: Modifier = Modifier
) {
    val durationText = HudFormatters.formatDuration(remainingDurationMinutes)
    val distanceText = if (remainingDistanceKm > 0.0) "${remainingDistanceKm} km" else "--"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HudColors.SurfaceCard)
            .border(1.dp, HudColors.CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "剩餘: $distanceText",
                color = HudColors.PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "抵達: $etaFormatted",
                color = HudColors.EmeraldGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "預計: $durationText",
                    color = HudColors.TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isOfflineMode) {
                    Text(
                        text = "⚡ 離線接續",
                        color = HudColors.VividAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Portrait Action Bar spanning full screen width.
 * Guarantees minimum 48–56dp touch targets without horizontal clipping.
 *
 * Spec reference: Review H3 requirement.
 */
@Composable
fun PortraitActionBar(
    vehicleType: VehicleType,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onCycleVehicleType: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicleBadge = HudFormatters.formatVehicleTag(vehicleType)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vehicle Type Mode Selector (Takes flexible width, height >= 48dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(HudColors.SurfaceCardElevated)
                .border(1.dp, HudColors.AzureBlueSubtle, RoundedCornerShape(8.dp))
                .clickable(onClick = onCycleVehicleType)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$vehicleBadge 切換",
                color = HudColors.PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        // Mute Button (min 48dp x 48dp touch target)
        Box(
            modifier = Modifier
                .size(48.dp)
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

        // Reset / Recenter Button (min 48dp x 48dp touch target)
        Box(
            modifier = Modifier
                .size(48.dp)
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
