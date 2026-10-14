package app.garminlike.nav.core.interfaces

import app.garminlike.nav.core.model.*

interface ISafetyEngine {
    /** Never alert solely because a point is within a straight-line radius. */
    fun evaluate(sample: LocationSample, route: NavRoute?): List<SafetyAlert>
}
