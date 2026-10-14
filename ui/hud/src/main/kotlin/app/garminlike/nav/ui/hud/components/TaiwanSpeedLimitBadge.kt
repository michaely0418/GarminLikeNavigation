package app.garminlike.nav.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Regulatory circular Taiwan speed limit sign (CNS 1438 / 標誌標線號誌設置規則 第85條).
 * Circular sign with white background, thick red circular ring, and bold black numerals.
 */
@Composable
fun TaiwanSpeedLimitBadge(
    limitKmh: Int?,
    isOverspeed: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val borderWidth = size * 0.12f
    val outerGlowModifier = if (isOverspeed) {
        modifier
            .size(size)
            .border(width = 3.dp, color = HudColors.CrimsonRed, shape = CircleShape)
    } else {
        modifier.size(size)
    }

    Box(
        modifier = outerGlowModifier
            .clip(CircleShape)
            .background(HudColors.TaiwanSpeedSignBg)
            .border(width = borderWidth, color = HudColors.TaiwanSpeedSignBorder, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val textValue = limitKmh?.toString() ?: "--"
        val fontSize = (size.value * 0.42f).sp

        Text(
            text = textValue,
            color = HudColors.TaiwanSpeedSignText,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}
