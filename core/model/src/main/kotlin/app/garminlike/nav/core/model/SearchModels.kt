package app.garminlike.nav.core.model

/** Retention and permitted downstream use must be set per provider agreement. */
enum class RetentionPolicy { NO_CACHE, SESSION_ONLY, PER_PROVIDER_POLICY, PERMITTED_PERSISTENCE }
enum class CoordinateUsePolicy { PROVIDER_RESTRICTED, THIRD_PARTY_USE_VERIFIED, UNKNOWN }

data class SearchRequest(val query: String, val locationBias: GeoPoint? = null, val limit: Int = 5) {
    init { require(query.isNotBlank()); require(limit in 1..20) }
}

data class PlaceCandidate(
    val providerId: String,
    val providerPlaceId: String,
    val displayName: String,
    val coordinate: GeoPoint?,
    val address: String?,
    val attribution: List<String>,
    val retentionPolicy: RetentionPolicy,
    val coordinateUsePolicy: CoordinateUsePolicy,
    val policyReference: String?
) {
    init { require(providerId.isNotBlank()); require(providerPlaceId.isNotBlank()); require(displayName.isNotBlank()) }
}
