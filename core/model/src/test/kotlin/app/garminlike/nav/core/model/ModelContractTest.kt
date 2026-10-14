package app.garminlike.nav.core.model

import org.junit.Assert.*
import org.junit.Test

class ModelContractTest {
    @Test fun validCoordinates() {
        assertEquals(25.0, GeoPoint(25.0, 121.0).latitudeDeg, 0.001)
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectInvalidLatitude() { GeoPoint(91.0, 121.0) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectInvalidSpeed() { LocationSample(GeoPoint(25.0, 121.0), 1L, speedMps = -1.0) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectBearing360() { LocationSample(GeoPoint(25.0, 121.0), 1L, bearingDeg = 360.0) }
    @Test fun unknownRoadDoesNotBecomeAllowed() {
        assertNotEquals(RoadAccessStatus.ALLOWED, RoadAccessStatus.UNKNOWN)
    }
    @Test fun forbidCannotBeSilentlyIgnored() {
        val report = LegalityReport(forbiddenSegmentIds = listOf("segment-1"))
        assertTrue(report.hasViolation)
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectInvalidAlertConfidence() {
        SafetyAlert("a", SafetyAlertKind.SPEED_CAMERA, "src", null, null, null, null, null, 1.2, "wrong", false)
    }
}
