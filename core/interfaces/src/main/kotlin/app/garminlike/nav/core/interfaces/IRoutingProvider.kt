package app.garminlike.nav.core.interfaces

import app.garminlike.nav.core.model.*

interface IRoutingProvider {
    val providerId: String
    /** Results containing forbidden segments MUST be rejected, regardless of route cost. */
    suspend fun calculateRoutes(request: RouteRequest): RouteResult
}
