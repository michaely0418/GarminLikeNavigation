package app.garminlike.nav.core.model

/** WGS84 coordinates in decimal degrees; not a map-provider license grant. */
data class GeoPoint(val latitudeDeg: Double, val longitudeDeg: Double) {
    init {
        require(latitudeDeg.isFinite() && latitudeDeg in -90.0..90.0) { "Invalid latitude" }
        require(longitudeDeg.isFinite() && longitudeDeg in -180.0..180.0) { "Invalid longitude" }
    }
}

data class LocationSample(
    val point: GeoPoint,
    val epochMillis: Long,
    val speedMps: Double? = null,
    val bearingDeg: Double? = null,
    val horizontalAccuracyMeters: Double? = null
) {
    init {
        require(epochMillis >= 0) { "Negative epoch" }
        require(speedMps == null || (speedMps.isFinite() && speedMps >= 0.0))
        require(bearingDeg == null || (bearingDeg.isFinite() && bearingDeg >= 0.0 && bearingDeg < 360.0))
        require(horizontalAccuracyMeters == null || (horizontalAccuracyMeters.isFinite() && horizontalAccuracyMeters >= 0.0))
    }
}
