package app.garminlike.nav

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import app.garminlike.nav.core.model.GeoPoint
import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.core.navigation.VehicleModeState
import app.garminlike.nav.ui.map.NavigationMapView
import java.util.Locale

/** Stage 1 map/location demonstration. This is NOT a driving navigation app yet. */
class MainActivity : Activity() {
    companion object {
        private const val LOCATION_REQUEST = 7101
        private const val PREFS = "navigation_preview"
        private const val MODE_KEY = "vehicle_type"
        private const val NIGHT_KEY = "map_night"
        private const val PROMPTED_KEY = "location_permission_prompted"
    }

    private val preferences by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private lateinit var mapWidget: NavigationMapView
    private lateinit var gpsStatus: TextView
    private lateinit var themeButton: Button
    private val modeButtons = mutableMapOf<VehicleType, Button>()
    private lateinit var modeState: VehicleModeState
    private var nightMode = false
    private var subscriptionsStarted = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0) return
            val point = GeoPoint(location.latitude, location.longitude)
            mapWidget.showLocation(point)
            val accuracy = if (location.hasAccuracy()) " / ±" + location.accuracy.toInt() + "m" else ""
            gpsStatus.text = String.format(
                Locale.TAIWAN, "GPS %.5f, %.5f%s", point.latitudeDeg, point.longitudeDeg, accuracy
            )
        }

        override fun onProviderDisabled(provider: String) {
            if (!hasEnabledProvider()) gpsStatus.text = "定位服務已關閉，請開啟手機定位"
        }

        override fun onProviderEnabled(provider: String) {
            if (hasAnyLocationPermission()) startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modeState = VehicleModeState(
            runCatching {
                VehicleType.valueOf(preferences.getString(MODE_KEY, VehicleType.CAR.name)!!)
            }.getOrDefault(VehicleType.CAR)
        )
        nightMode = preferences.getBoolean(NIGHT_KEY, false)
        mapWidget = NavigationMapView(this)
        mapWidget.setNightMode(nightMode)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(20, 27, 37))
        }
        root.addView(label("Garmin-like Navigation   •   Stage 1", 19f).apply {
            setPadding(dp(16), dp(10), dp(8), dp(2))
        })
        root.addView(label("地圖與定位預覽｜尚無導航及測速功能", 12f).apply {
            setTextColor(Color.rgb(239, 195, 123))
            setPadding(dp(16), 0, 0, dp(8))
        })
        root.addView(vehicleSelector())
        root.addView(mapWidget, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(mapActions())
        gpsStatus = label("等待定位權限", 13f).apply {
            setPadding(dp(14), dp(6), dp(14), dp(6))
        }
        root.addView(gpsStatus)
        root.addView(label("圖資：OpenFreeMap / OpenStreetMap • 需網路 • 僅供測試", 11f).apply {
            setTextColor(Color.LTGRAY)
            setPadding(dp(14), 0, dp(14), dp(9))
        })
        setContentView(root)
        mapWidget.onCreate(savedInstanceState)
        updateVehicleButtons()

        if (!hasAnyLocationPermission() && !preferences.getBoolean(PROMPTED_KEY, false)) {
            preferences.edit().putBoolean(PROMPTED_KEY, true).apply()
            requestLocationPermissions()
        }
    }

    private fun vehicleSelector(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), 0, dp(8), dp(8))
            val titles = listOf(
                VehicleType.CAR to "汽車",
                VehicleType.SCOOTER to "機車",
                VehicleType.HEAVY_MOTORCYCLE to "大型重機"
            )
            titles.forEach { (type, title) ->
                val button = actionButton(title) {
                    modeState.select(type)
                    preferences.edit().putString(MODE_KEY, type.name).apply()
                    updateVehicleButtons()
                }
                modeButtons[type] = button
                addView(button, LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                    setMargins(dp(2), 0, dp(2), 0)
                })
            }
        }
    }

    private fun updateVehicleButtons() {
        modeButtons.forEach { (type, button) ->
            val selected = modeState.mode.value == type
            button.backgroundTintList = ColorStateList.valueOf(
                if (selected) Color.rgb(25, 103, 186) else Color.rgb(66, 76, 87)
            )
            button.setTextColor(Color.WHITE)
            button.isSelected = selected
        }
    }

    private fun mapActions(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(6), dp(8), 0)
            listOf(
                actionButton("＋") { mapWidget.zoomIn() },
                actionButton("－") { mapWidget.zoomOut() },
                actionButton("◎ 定位") {
                    if (!hasAnyLocationPermission()) requestLocationPermissions()
                    else if (!mapWidget.centerOnCurrentLocation()) {
                        gpsStatus.text = "正在等待 GPS 定位，請確認手機定位服務已開啟"
                        startLocationUpdates()
                    }
                }
            ).forEach { button ->
                addView(button, LinearLayout.LayoutParams(0, dp(50), 1f).apply {
                    setMargins(dp(2), 0, dp(2), 0)
                })
            }
            themeButton = actionButton("") {
                nightMode = !nightMode
                preferences.edit().putBoolean(NIGHT_KEY, nightMode).apply()
                mapWidget.setNightMode(nightMode)
                updateThemeButton()
            }
            updateThemeButton()
            addView(themeButton, LinearLayout.LayoutParams(0, dp(50), 1f).apply {
                setMargins(dp(2), 0, dp(2), 0)
            })
        }
    }

    private fun updateThemeButton() {
        themeButton.text = if (nightMode) "☀ 日間" else "☾ 夜間"
    }

    private fun label(content: String, sizeSp: Float): TextView =
        TextView(this).apply {
            text = content
            textSize = sizeSp
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
        }

    private fun actionButton(title: String, click: () -> Unit): Button =
        Button(this).apply {
            text = title
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setOnClickListener { click() }
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun hasAnyLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasFineLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_REQUEST
        )
    }

    private fun hasEnabledProvider(): Boolean =
        (runCatching { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ||
         runCatching { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false))

    private fun startLocationUpdates() {
        if (!hasAnyLocationPermission()) {
            gpsStatus.text = "未取得定位權限；點選「◎ 定位」授權"
            return
        }
        if (!hasEnabledProvider()) {
            gpsStatus.text = "定位服務已關閉，請開啟手機定位"
            return
        }
        if (subscriptionsStarted) return
        try {
            var count = 0
            val fine = hasFineLocationPermission()
            if (fine && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 3f, listener)
                count++
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 5f, listener)
                count++
            }
            subscriptionsStarted = count > 0
            if (!subscriptionsStarted) {
                gpsStatus.text = "此裝置目前沒有可用的定位提供者"
            } else {
                gpsStatus.text = "定位中…（可拖曳地圖，按「◎ 定位」重新置中）"
                val providers = if (fine) listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                                else listOf(LocationManager.NETWORK_PROVIDER)
                providers.mapNotNull { provider ->
                    runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
                }.filter { System.currentTimeMillis() - it.time in 0L..300_000L }
                 .maxByOrNull { it.time }?.let(listener::onLocationChanged)
            }
        } catch (_: SecurityException) {
            gpsStatus.text = "定位授權遭撤銷；請重新授權"
            stopLocationUpdates()
        } catch (_: IllegalArgumentException) {
            gpsStatus.text = "定位來源無法啟用，請檢查手機設定"
            stopLocationUpdates()
        }
    }

    private fun stopLocationUpdates() {
        if (!subscriptionsStarted) return
        runCatching { locationManager.removeUpdates(listener) }
        subscriptionsStarted = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST) {
            if (hasAnyLocationPermission()) startLocationUpdates()
            else gpsStatus.text = "定位權限被拒絕；可點選「◎ 定位」再次授權"
        }
    }

    override fun onStart() { super.onStart(); mapWidget.onStart() }
    override fun onResume() { super.onResume(); mapWidget.onResume(); startLocationUpdates() }
    override fun onPause() { stopLocationUpdates(); mapWidget.onPause(); super.onPause() }
    override fun onStop() { mapWidget.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapWidget.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        mapWidget.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() { mapWidget.onDestroy(); super.onDestroy() }
}
