package app.garminlike.nav.core.model

enum class ManeuverType { DEPART, STRAIGHT, TURN_LEFT, TURN_RIGHT, KEEP_LEFT, KEEP_RIGHT, U_TURN, ARRIVE, UNKNOWN }

data class Maneuver(val type: ManeuverType, val at: GeoPoint, val instruction: String, val distanceFromStartMeters: Double) {
    init { require(distanceFromStartMeters.isFinite() && distanceFromStartMeters >= 0.0) }
}

data class LegalityReport(
    val forbiddenSegmentIds: List<String> = emptyList(),
    val unknownSegmentIds: List<String> = emptyList(),
    val evidenceIds: List<String> = emptyList()
) {
    val hasViolation: Boolean get() = forbiddenSegmentIds.isNotEmpty()
    val hasUnknownAccess: Boolean get() = unknownSegmentIds.isNotEmpty()
}

data class NavRoute(
    val routeId: String,
    val vehicleType: VehicleType,
    val geometry: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val maneuvers: List<Maneuver> = emptyList(),
    val legality: LegalityReport = LegalityReport(),
    val costExplanation: Map<String, Double> = emptyMap(),
    val routingEngineVersion: String,
    val ruleSetVersion: String
) {
    init {
        require(routeId.isNotBlank())
        require(geometry.size >= 2)
        require(distanceMeters.isFinite() && distanceMeters >= 0.0)
        require(durationSeconds >= 0)
        require(costExplanation.values.all { it.isFinite() })
    }
}

data class RouteRequest(
    val origin: GeoPoint,
    val destination: GeoPoint,
    val waypoints: List<GeoPoint> = emptyList(),
    val vehicleType: VehicleType,
    val preferences: RoutingPreferences = RoutingPreferences(),
    val avoidedSegmentIds: Set<String> = emptySet()
)

data class RouteResult(
    val candidates: List<NavRoute>,
    val mapDataVersion: String,
    val engineVersion: String,
    val generatedAtEpochMillis: Long,
    val note: String? = null
)
