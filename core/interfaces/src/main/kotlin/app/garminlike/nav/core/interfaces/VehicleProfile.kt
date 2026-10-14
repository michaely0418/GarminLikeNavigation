package app.garminlike.nav.core.interfaces

import app.garminlike.nav.core.model.*

/** Statutory legality must be checked before any route preference/cost optimization. */
interface VehicleProfile {
    val type: VehicleType
    val ruleSetVersion: String
    fun evaluateRoadAccess(segment: RoadSegmentDescriptor): RoadAccessDecision
    fun defaultPreferences(): RoutingPreferences
}
