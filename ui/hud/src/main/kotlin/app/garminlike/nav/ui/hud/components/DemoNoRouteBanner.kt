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
import app.garminlike.nav.ui.hud.model.DemoModeSelection
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Prominent DEMO / NO ROUTE notice required for Stage 1.
 * Prevents misleading drivers with unverified simulated telemetry while
 * providing an interactive demonstration of Car, Scooter, and Heavy Motorcycle modes.
 *
 * Spec reference: GitHub Issue #4 & Review H3/H4 requirements.
 */
@Composable
fun DemoNoRouteBanner(
    currentSelection: DemoModeSelection,
    onSelectUnroutedDemo: () -> Unit,
    onSelectCarDemo: () -> Unit,
    onSelectScooterDemo: () -> Unit,
    onSelectHeavyMotorcycleDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnrouted = currentSelection == DemoModeSelection.UNROUTED

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HudColors.SurfaceCardElevated)
            .border(1.dp, HudColors.AzureBlueSubtle, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stage 1 Demo Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isUnrouted) HudColors.VividAmber else HudColors.AzureBlue)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isUnrouted) "⚡ DEMO / NO ROUTE" else "⚡ DEMO SIMULATION",
                        color = HudColors.PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "Stage 1 HUD 原型 (未連線引擎)",
                    color = HudColors.TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Demo Mode Quick-Switch Buttons for Interactive Testing (touch height >= 48dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DemoModeButton(
                    label = "無路線",
                    isActive = currentSelection == DemoModeSelection.UNROUTED,
                    onClick = onSelectUnroutedDemo,
                    modifier = Modifier.weight(1f)
                )
                DemoModeButton(
                    label = "汽車",
                    isActive = currentSelection == DemoModeSelection.CAR,
                    onClick = onSelectCarDemo,
                    modifier = Modifier.weight(1f)
                )
                DemoModeButton(
                    label = "機車",
                    isActive = currentSelection == DemoModeSelection.SCOOTER,
                    onClick = onSelectScooterDemo,
                    modifier = Modifier.weight(1f)
                )
                DemoModeButton(
                    label = "重機",
                    isActive = currentSelection == DemoModeSelection.HEAVY_MOTORCYCLE,
                    onClick = onSelectHeavyMotorcycleDemo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DemoModeButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) HudColors.AzureBlue else HudColors.SurfaceCard
    val textColor = if (isActive) HudColors.BackgroundBlack else HudColors.PureWhite

    Box(
        modifier = modifier
            .heightIn(min = 48.dp) // Accessible driving touch target per H3
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(
                1.dp,
                if (isActive) HudColors.AzureBlue else HudColors.CardBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
