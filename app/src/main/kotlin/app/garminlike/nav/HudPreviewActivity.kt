package app.garminlike.nav

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.ui.hud.InteractiveHudDemo
import app.garminlike.nav.ui.hud.model.NavHudUiState
import app.garminlike.nav.ui.hud.theme.GarminHudTheme
import app.garminlike.nav.ui.hud.theme.HudColors

/**
 * Phone HUD DEMO entry point for Stage 1 integration testing.
 * This screen intentionally has no live guidance, speed camera, map overlay
 * or Android Auto projection. The HUD's map viewport is a placeholder.
 */
class HudPreviewActivity : ComponentActivity() {
    companion object {
        private const val PREFS = "navigation_preview"
        private const val MODE_KEY = "vehicle_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val initialVehicle = runCatching {
            VehicleType.valueOf(preferences.getString(MODE_KEY, VehicleType.CAR.name)!!)
        }.getOrDefault(VehicleType.CAR)

        setContent {
            GarminHudTheme {
                HudPreviewContent(
                    initialVehicle = initialVehicle,
                    onBack = { finish() },
                    onVehicleChanged = { mode ->
                        preferences.edit().putString(MODE_KEY, mode.name).apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun HudPreviewContent(
    initialVehicle: VehicleType,
    onBack: () -> Unit,
    onVehicleChanged: (VehicleType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HudColors.BackgroundBlack)
            .safeDrawingPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("‹ 返回地圖", color = HudColors.AzureBlue, fontSize = 15.sp)
            }
            Text("HUD 展示 • 非即時導航", color = HudColors.PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        InteractiveHudDemo(
            initialState = NavHudUiState.demoInitial(initialVehicle),
            onVehicleTypeChanged = onVehicleChanged
        )
    }
}
