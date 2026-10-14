package app.garminlike.nav.core.model

enum class VehicleType { CAR, SCOOTER, HEAVY_MOTORCYCLE }
enum class RoadAccessStatus { ALLOWED, FORBIDDEN, UNKNOWN }

/** Law-based access is a hard constraint; the cost model MUST NOT override FORBIDDEN. */
data class RoadAccessDecision(
    val status: RoadAccessStatus,
    val evidenceIds: List<String> = emptyList(),
    val reason: String,
    val ruleSetVersion: String
)

data class RoadSegmentDescriptor(
    val segmentId: String,
    val osmHighwayTag: String?,
    val attributes: Map<String, String> = emptyMap()
)

data class RoutingPreferences(
    val minorRoadPenalty: Double = 1.0,
    val distanceInfluence: Double = 1.0,
    val turnPenalty: Double = 1.0,
    val allowTolls: Boolean = true,
    val preferMajorRoads: Boolean = true
) {
    init {
        require(minorRoadPenalty.isFinite() && minorRoadPenalty >= 0)
        require(distanceInfluence.isFinite() && distanceInfluence >= 0)
        require(turnPenalty.isFinite() && turnPenalty >= 0)
    }
}
