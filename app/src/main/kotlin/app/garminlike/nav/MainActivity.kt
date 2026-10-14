package app.garminlike.nav

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

/** Stage 0 launch smoke screen. No working map, routing, GPS, or Android Auto yet. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "Garmin-like Navigation\nStage 0: project structure ready\n\nRouting, map, safety and Android Auto are not yet implemented."
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        })
    }
}
