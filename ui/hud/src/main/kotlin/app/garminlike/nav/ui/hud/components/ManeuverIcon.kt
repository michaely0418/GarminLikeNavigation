package app.garminlike.nav.ui.hud.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.garminlike.nav.core.model.ManeuverType
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * High-visibility directional vector arrows for navigation maneuvers.
 * Designed for immediate cognitive recognition at highway speeds.
 */
@Composable
fun ManeuverIcon(
    maneuverType: ManeuverType?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    tint: Color = HudColors.EmeraldGreen
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.16f
        val strokeStyle = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val path = Path()

        when (maneuverType) {
            ManeuverType.STRAIGHT -> {
                // Upward arrow
                path.moveTo(w * 0.5f, h * 0.85f)
                path.lineTo(w * 0.5f, h * 0.25f)
                drawPath(path, color = tint, style = strokeStyle)

                val head = Path().apply {
                    moveTo(w * 0.25f, h * 0.45f)
                    lineTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.75f, h * 0.45f)
                }
                drawPath(head, color = tint, style = strokeStyle)
            }
            ManeuverType.TURN_LEFT -> {
                // Up and 90 degree turn to the left
                path.moveTo(w * 0.75f, h * 0.85f)
                path.lineTo(w * 0.75f, h * 0.5f)
                path.quadraticTo(w * 0.75f, h * 0.35f, w * 0.6f, h * 0.35f)
                path.lineTo(w * 0.3f, h * 0.35f)
                drawPath(path, color = tint, style = strokeStyle)

                val head = Path().apply {
                    moveTo(w * 0.45f, h * 0.15f)
                    lineTo(w * 0.15f, h * 0.35f)
                    lineTo(w * 0.45f, h * 0.55f)
                }
                drawPath(head, color = tint, style = strokeStyle)
            }
            ManeuverType.TURN_RIGHT -> {
                // Up and 90 degree turn to the right
                path.moveTo(w * 0.25f, h * 0.85f)
                path.lineTo(w * 0.25f, h * 0.5f)
                path.quadraticTo(w * 0.25f, h * 0.35f, w * 0.4f, h * 0.35f)
                path.lineTo(w * 0.7f, h * 0.35f)
                drawPath(path, color = tint, style = strokeStyle)

                val head = Path().apply {
                    moveTo(w * 0.55f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.35f)
                    lineTo(w * 0.55f, h * 0.55f)
                }
                drawPath(head, color = tint, style = strokeStyle)
            }
            ManeuverType.KEEP_LEFT -> {
                // Angled fork to the left
                path.moveTo(w * 0.65f, h * 0.85f)
                path.lineTo(w * 0.65f, h * 0.6f)
                path.lineTo(w * 0.35f, h * 0.3f)
                drawPath(path, color = tint, style = strokeStyle)

                val head = Path().apply {
                    moveTo(w * 0.25f, h * 0.45f)
                    lineTo(w * 0.25f, h * 0.2f)
                    lineTo(w * 0.5f, h * 0.2f)
                }
                drawPath(head, color = tint, style = strokeStyle)
            }
            ManeuverType.KEEP_RIGHT -> {
                // Angled fork to the right
                path.moveTo(w * 0.35f, h * 0.85f)
                path.lineTo(w * 0.35f, h * 0.6f)
                path.lineTo(w * 0.65f, h * 0.3f)
                drawPath(path, color = tint, style = strokeStyle)

                val head = Path().apply {
                    moveTo(w * 0.5f, h * 0.2f)
                    lineTo(w * 0.75f, h * 0.2f)
                    lineTo(w * 0.75f, h * 0.45f)
                }
                drawPath(head, color = tint, style = strokeStyle)
            }
            ManeuverType.U_TURN -> {
                // Hairpin turn around to the left
                path.moveTo(w * 0.75f, h * 0.85f)
                path.lineTo(w * 0.75f, h * 0.45f)
                path.quadraticTo(w * 0.75f, h * 0.15f, w * 0.5f, h * 0.15f)
                path.quadraticTo(w * 0.25f, h * 0.15f, w * 0.25f, h * 0.45f)
                path.lineTo(w * 0.25f, h * 0.75f)
                drawPath(path, color = tint, style = strokeStyle)

                val head = Path().apply {
                    moveTo(w * 0.1f, h * 0.6f)
                    lineTo(w * 0.25f, h * 0.85f)
                    lineTo(w * 0.4f, h * 0.6f)
                }
                drawPath(head, color = tint, style = strokeStyle)
            }
            ManeuverType.ARRIVE -> {
                // Destination pin / bullseye
                drawCircle(
                    color = tint,
                    radius = w * 0.35f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = strokeStyle
                )
                drawCircle(
                    color = tint,
                    radius = w * 0.15f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
            ManeuverType.DEPART, ManeuverType.UNKNOWN, null -> {
                // Upward navigation chevron
                val head = Path().apply {
                    moveTo(w * 0.2f, h * 0.65f)
                    lineTo(w * 0.5f, h * 0.3f)
                    lineTo(w * 0.8f, h * 0.65f)
                }
                drawPath(head, color = tint, style = strokeStyle)
            }
        }
    }
}
