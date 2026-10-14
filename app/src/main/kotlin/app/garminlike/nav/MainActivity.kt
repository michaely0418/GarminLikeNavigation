package app.garminlike.nav

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import app.garminlike.nav.core.model.GeoPoint
import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.core.navigation.LocationFixPolicy
import app.garminlike.nav.core.navigation.LocationFixQuality
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
    private lateinit var connectivityStatus: TextView
    private lateinit var themeButton: Button
    private val modeButtons = mutableMapOf<VehicleType, Button>()
    private lateinit var modeState: VehicleModeState
    private var nightMode = false
    private var subscriptionsStarted = false
    private var lastDisplayedLocation: Location? = null

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (!location.latitude.isFinite() || !location.longitude.isFinite()) return
            if (location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0) return
            // Network and GPS callbacks can arrive out of order. Prefer the
            // monotonic fix timestamp; keep the earlier accuracy safeguard.
            val previousQuality = lastDisplayedLocation?.let {
                LocationFixQuality(
                    fixTimeMillis = it.time,
                    elapsedRealtimeNanos = it.elapsedRealtimeNanos,
                    accuracyMeters = if (it.hasAccuracy()) it.accuracy else null
                )
            }
            val incomingQuality = LocationFixQuality(
                fixTimeMillis = location.time,
                elapsedRealtimeNanos = location.elapsedRealtimeNanos,
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else null
            )
            if (!LocationFixPolicy.shouldAccept(previousQuality, incomingQuality, System.currentTimeMillis())) {
                return
            }

            lastDisplayedLocation = Location(location)
            val point = GeoPoint(location.latitude, location.longitude)
            mapWidget.showLocation(point)
            val source = when {
                location.provider == LocationManager.GPS_PROVIDER -> "GPS"
                !hasFineLocationPermission() -> "概略定位"
                else -> "網路定位"
            }
            val accuracy = if (location.hasAccuracy()) " / ±" + location.accuracy.toInt() + "m" else ""
            val hint = if (location.hasAccuracy() && location.accuracy >= 50f) "（精度有限）" else ""
            gpsStatus.text = String.format(
                Locale.TAIWAN, "%s %.5f, %.5f%s%s",
                source, point.latitudeDeg, point.longitudeDeg, accuracy, hint
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

        val isLandscape = resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE

        val root = LinearLayout(this).apply {
            orientation = if (isLandscape) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(20, 27, 37))
            // Android 15+ edge-to-edge; apply safe insets to BOTH arrangements.
            setOnApplyWindowInsetsListener { view, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val safe = insets.getInsets(
                        WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
                    )
                    view.setPadding(safe.left, safe.top, safe.right, safe.bottom)
                } else {
                    @Suppress("DEPRECATION")
                    view.setPadding(
                        insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight, insets.systemWindowInsetBottom
                    )
                }
                insets
            }
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(label("Garmin-like Navigation   •   Stage 1", 19f).apply {
                setPadding(dp(16), dp(10), dp(8), dp(2))
            })
            addView(label("地圖與定位預覽｜尚無導航及測速功能", 12f).apply {
                setTextColor(Color.rgb(239, 195, 123))
                setPadding(dp(16), 0, 0, dp(8))
            })
            addView(vehicleSelector())
            addView(actionButton("HUD 展示（模擬資料）") {
                startActivity(Intent(this@MainActivity, HudPreviewActivity::class.java))
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)).apply {
                setMargins(dp(10), 0, dp(10), dp(6))
            })
        }

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(mapActions())
        }
        gpsStatus = label("等待定位權限", 13f).apply {
            setPadding(dp(14), dp(6), dp(14), dp(6))
        }
        footer.addView(gpsStatus)
        connectivityStatus = label("正在檢查網路狀態", 12f).apply {
            setTextColor(Color.rgb(219, 210, 175))
            setPadding(dp(14), 0, dp(14), dp(4))
        }
        footer.addView(connectivityStatus)
        footer.addView(label("圖資：OpenFreeMap / OpenStreetMap • 需網路 • 僅供測試", 11f).apply {
            setTextColor(Color.LTGRAY)
            setPadding(dp(14), 0, dp(14), dp(9))
        })

        if (isLandscape) {
            // Landscape is map-first: fixed-width, SCROLLABLE controls on the right.
            // A vertical toolbar above the map used to leave only a 20–40dp map strip.
            root.addView(mapWidget, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f
            ))
            val controls = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                addView(header)
                addView(footer)
            }
            val scrollPanel = android.widget.ScrollView(this).apply {
                isFillViewport = false
                addView(controls)
                setBackgroundColor(Color.rgb(20, 27, 37))
            }
            // Support split screen / small landscape devices without wasting map width.
            val panelWidthDp = (resources.configuration.screenWidthDp * 0.40f)
                .toInt().coerceIn(260, 360)
            root.addView(scrollPanel, LinearLayout.LayoutParams(
                dp(panelWidthDp), ViewGroup.LayoutParams.MATCH_PARENT
            ))
        } else {
            // Portrait: toolbar at top, map grows to remaining height.
            root.addView(header)
            root.addView(mapWidget, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            ))
            root.addView(footer)
        }
        setContentView(root)
        mapWidget.onCreate(savedInstanceState)
        updateVehicleButtons()
        updateConnectivityStatus()

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
            // Narrow screens and larger system fonts need compact button padding.
            minWidth = 0
            setPadding(dp(2), 0, dp(2), 0)
            // Theme.Material.Light's default button is light grey: white text on
            // it is effectively invisible. Force an accessible dark action tint.
            backgroundTintList = ColorStateList.valueOf(Color.rgb(43, 68, 95))
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

    private fun updateConnectivityStatus() {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capability = manager.getNetworkCapabilities(manager.activeNetwork)
        connectivityStatus.text = if (capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
            "網路已連線；若地圖空白，請檢查圖資服務"
        } else {
            "離線：目前無可用網路，線上底圖可能無法載入"
        }
    }

    override fun onStart() { super.onStart(); mapWidget.onStart() }
    override fun onResume() {
        super.onResume()
        mapWidget.onResume()
        // HUD preview stores the same vehicle type in SharedPreferences.
        val persisted = runCatching {
            VehicleType.valueOf(preferences.getString(MODE_KEY, VehicleType.CAR.name)!!)
        }.getOrDefault(VehicleType.CAR)
        if (modeState.mode.value != persisted) {
            modeState.select(persisted)
            updateVehicleButtons()
        }
        updateConnectivityStatus()
        startLocationUpdates()
    }
    override fun onPause() { stopLocationUpdates(); mapWidget.onPause(); super.onPause() }
    override fun onStop() { mapWidget.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapWidget.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        mapWidget.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() { mapWidget.onDestroy(); super.onDestroy() }
}
