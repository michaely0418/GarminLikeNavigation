package app.garminlike.nav.core.model

enum class SafetyAlertKind { SPEED_CAMERA, SECTION_SPEED, RED_LIGHT, TRAFFIC_ENFORCEMENT, SPEED_LIMIT, ROAD_RESTRICTION }

data class SafetyAlert(
    val alertId: String,
    val kind: SafetyAlertKind,
    val sourceId: String,
    val sourcePublishedAtEpochMillis: Long?,
    val expiresAtEpochMillis: Long?,
    val cameraBearingDeg: Double?,
    val directionMatch: Boolean?,
    val distanceAlongRouteMeters: Double?,
    val confidence: Double,
    val message: String,
    val routeCorrelated: Boolean,
    val evidenceIds: List<String> = emptyList()
) {
    init {
        require(alertId.isNotBlank()); require(sourceId.isNotBlank())
        require(confidence.isFinite() && confidence in 0.0..1.0)
        require(cameraBearingDeg == null || (cameraBearingDeg.isFinite() && cameraBearingDeg >= 0 && cameraBearingDeg < 360))
        require(distanceAlongRouteMeters == null || (distanceAlongRouteMeters.isFinite() && distanceAlongRouteMeters >= 0))
    }
}
