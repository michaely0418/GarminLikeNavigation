package app.garminlike.nav.core.interfaces

import app.garminlike.nav.core.model.*

interface ISearchProvider {
    val providerId: String
    /** Consumers MUST respect PlaceCandidate retention/coordinate-use policy. */
    suspend fun search(request: SearchRequest): List<PlaceCandidate>
}
