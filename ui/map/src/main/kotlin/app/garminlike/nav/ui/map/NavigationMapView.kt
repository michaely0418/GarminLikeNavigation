package app.garminlike.nav.ui.map

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import app.garminlike.nav.core.model.GeoPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

/**
 * Stage 1 phone-only MapLibre surface.
 * No turn-by-turn, route calculation or Google Places data is processed here.
 *
 * The SDK's attribution control must remain visible; the hosting Activity keeps
 * all bottom controls OUTSIDE the map viewport so they cannot cover attribution.
 */
class NavigationMapView(context: Context) : FrameLayout(context) {
    companion object {
        const val DAY_STYLE = "https://tiles.openfreemap.org/styles/liberty"
        const val NIGHT_STYLE = "https://tiles.openfreemap.org/styles/dark"
        private val TAIPEI = LatLng(25.0330, 121.5654)
    }

    private val mapView: MapView
    private var map: MapLibreMap? = null
    private var locationMarker: Marker? = null
    private var currentLocation: GeoPoint? = null
    private var centeredOnFirstFix = false
    private var night = false
    private var created = false

    init {
        // Must be initialized BEFORE constructing MapView (MapLibre SDK contract).
        MapLibre.getInstance(context.applicationContext)
        mapView = MapView(context)
        addView(mapView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun onCreate(savedInstanceState: Bundle?) {
        if (created) return
        created = true
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { readyMap ->
            map = readyMap
            // MapView.onCreate(savedInstanceState) restores the user's camera.
            // Never override it on rotation: the user may have panned/zoomed
            // away from the live location before changing orientation.
            // Also prevent the first post-rotation GPS fix from auto-recentering.
            if (savedInstanceState != null) {
                centeredOnFirstFix = true
            } else {
                // On a fresh launch, location can arrive before getMapAsync.
                val fix = currentLocation
                readyMap.cameraPosition = CameraPosition.Builder()
                    .target(
                        if (fix == null) TAIPEI
                        else LatLng(fix.latitudeDeg, fix.longitudeDeg)
                    )
                    .zoom(if (fix == null) 10.5 else 16.0)
                    .build()
                if (fix != null) centeredOnFirstFix = true
            }
            applyStyle()
        }
    }

    /** Set a daytime or nighttime map style, without changing routing state. */
    fun setNightMode(enabled: Boolean) {
        if (night == enabled) return
        night = enabled
        if (map != null) applyStyle()
    }

    private fun applyStyle() {
        val activeMap = map ?: return
        locationMarker = null // Annotations are recreated after the new style loads.
        activeMap.setStyle(if (night) NIGHT_STYLE else DAY_STYLE) {
            showLocationMarker()
        }
    }

    fun showLocation(point: GeoPoint) {
        currentLocation = point
        showLocationMarker()
        if (!centeredOnFirstFix) {
            // Keep first-fix recenter pending until MapLibre is actually ready.
            // Previously this flag flipped when map==null and recenter was lost.
            centeredOnFirstFix = centerOnCurrentLocation()
        }
    }

    @Suppress("DEPRECATION") // Markers are for Stage 1 only; future stage uses a style layer.
    private fun showLocationMarker() {
        val activeMap = map ?: return
        val point = currentLocation ?: return
        // Do not add markers until at least one style has finished loading.
        if (activeMap.style == null) return
        locationMarker?.let { activeMap.removeMarker(it) }
        locationMarker = activeMap.addMarker(
            MarkerOptions()
                .position(LatLng(point.latitudeDeg, point.longitudeDeg))
                .title("目前位置")
        )
    }

    /** Explicit recenter does not force a follow mode on future GPS updates. */
    fun centerOnCurrentLocation(): Boolean {
        val point = currentLocation ?: return false
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(point.latitudeDeg, point.longitudeDeg), 16.0),
            500
        )
        return map != null
    }

    fun zoomIn() {
        map?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    fun zoomOut() {
        map?.animateCamera(CameraUpdateFactory.zoomOut())
    }

    fun onStart() { if (created) mapView.onStart() }
    fun onResume() { if (created) mapView.onResume() }
    fun onPause() { if (created) mapView.onPause() }
    fun onStop() { if (created) mapView.onStop() }
    fun onLowMemory() { if (created) mapView.onLowMemory() }
    fun onSaveInstanceState(outState: Bundle) {
        if (created) mapView.onSaveInstanceState(outState)
    }
    fun onDestroy() {
        if (created) {
            mapView.onDestroy()
            map = null
            locationMarker = null
            created = false
        }
    }
}
