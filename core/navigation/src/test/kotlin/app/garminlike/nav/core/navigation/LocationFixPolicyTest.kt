package app.garminlike.nav.core.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationFixPolicyTest {
    private val now = 1_800_000L
    private fun fix(
        time: Long = now,
        monotonic: Long = 10_000L,
        accuracy: Float? = 5f
    ) = LocationFixQuality(time, monotonic, accuracy)

    @Test fun firstFixIsAccepted() {
        assertTrue(LocationFixPolicy.shouldAccept(null, fix(), now))
    }

    @Test fun outOfOrderNetworkFixCannotReplaceNewerGpsFix() {
        val gps = fix(monotonic = 20_000L, accuracy = 5f)
        val delayedNetwork = fix(time = now - 1_000L, monotonic = 15_000L, accuracy = 7f)
        assertFalse(LocationFixPolicy.shouldAccept(gps, delayedNetwork, now))
    }

    @Test fun newerTimestampFromMonotonicClockWinsOverWallClockSkew() {
        val gps = fix(time = now, monotonic = 20_000L, accuracy = 8f)
        val network = fix(time = now - 1_000L, monotonic = 21_000L, accuracy = 7f)
        assertTrue(LocationFixPolicy.shouldAccept(gps, network, now))
    }

    @Test fun missingMonotonicTimestampFallsBackToWallClock() {
        assertFalse(
            LocationFixPolicy.shouldAccept(
                fix(time = now, monotonic = 0L),
                fix(time = now - 100L, monotonic = 0L),
                now
            )
        )
    }

    @Test fun substantiallyWorseAccuracyIsRejected() {
        assertFalse(LocationFixPolicy.shouldAccept(fix(accuracy = 5f), fix(monotonic = 20_000L, accuracy = 100f), now))
    }

    @Test fun slightlyWorseNewerFixIsAccepted() {
        assertTrue(LocationFixPolicy.shouldAccept(fix(accuracy = 5f), fix(monotonic = 20_000L, accuracy = 15f), now))
    }

    @Test fun previousFixOutsideFreshnessWindowDoesNotBlockNewFix() {
        assertTrue(
            LocationFixPolicy.shouldAccept(
                fix(time = now - 30_000L, monotonic = 20_000L),
                fix(monotonic = 10_000L),
                now
            )
        )
    }

    @Test fun unknownAccuracyDoesNotRejectAnOtherwiseValidUpdate() {
        assertTrue(LocationFixPolicy.shouldAccept(fix(accuracy = null), fix(monotonic = 20_000L), now))
    }
}
