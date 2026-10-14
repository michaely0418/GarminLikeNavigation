package app.garminlike.nav.ui.hud

import app.garminlike.nav.core.model.VehicleType
import app.garminlike.nav.ui.hud.model.HudFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HudFormattersTest {

    @Test
    fun `formatSpeed converts mps to kmh correctly`() {
        // 16.6667 m/s * 3.6 = 60 km/h
        assertEquals(60, HudFormatters.formatSpeed(16.6667))
        // 27.7778 m/s * 3.6 = 100 km/h
        assertEquals(100, HudFormatters.formatSpeed(27.7778))
        // 0 m/s = 0 km/h
        assertEquals(0, HudFormatters.formatSpeed(0.0))
    }

    @Test
    fun `formatSpeed handles null negative and non-finite values safely`() {
        assertEquals(0, HudFormatters.formatSpeed(null))
        assertEquals(0, HudFormatters.formatSpeed(-5.0))
        assertEquals(0, HudFormatters.formatSpeed(Double.NaN))
        assertEquals(0, HudFormatters.formatSpeed(Double.POSITIVE_INFINITY))
    }

    @Test
    fun `formatDistance formats meters under 1000m with 10m rounding`() {
        assertEquals("350 m", HudFormatters.formatDistance(352.0))
        assertEquals("80 m", HudFormatters.formatDistance(84.0))
        assertEquals("90 m", HudFormatters.formatDistance(86.0))
        assertEquals("0 m", HudFormatters.formatDistance(0.0))
    }

    @Test
    fun `formatDistance formats kilometers at or over 1000m with 1 decimal`() {
        assertEquals("1.0 km", HudFormatters.formatDistance(1000.0))
        assertEquals("4.8 km", HudFormatters.formatDistance(4820.0))
        assertEquals("12.5 km", HudFormatters.formatDistance(12500.0))
    }

    @Test
    fun `formatDistance handles null and invalid inputs gracefully`() {
        assertEquals("--", HudFormatters.formatDistance(null))
        assertEquals("--", HudFormatters.formatDistance(-100.0))
        assertEquals("--", HudFormatters.formatDistance(Double.NaN))
    }

    @Test
    fun `formatDuration formats minutes into localized Chinese text`() {
        assertEquals("8 分鐘", HudFormatters.formatDuration(8))
        assertEquals("45 分鐘", HudFormatters.formatDuration(45))
        assertEquals("1 小時", HudFormatters.formatDuration(60))
        assertEquals("1 小時 15 分", HudFormatters.formatDuration(75))
        assertEquals("2 小時 30 分", HudFormatters.formatDuration(150))
        assertEquals("--", HudFormatters.formatDuration(0))
        assertEquals("--", HudFormatters.formatDuration(-10))
    }

    @Test
    fun `formatVehicleName returns correct traditional Chinese labels`() {
        assertEquals("小客車", HudFormatters.formatVehicleName(VehicleType.CAR))
        assertEquals("普通機車", HudFormatters.formatVehicleName(VehicleType.SCOOTER))
        assertEquals("大型重機", HudFormatters.formatVehicleName(VehicleType.HEAVY_MOTORCYCLE))
    }

    @Test
    fun `formatVehicleTag returns expected emoji badges`() {
        assertEquals("[🚗 汽車]", HudFormatters.formatVehicleTag(VehicleType.CAR))
        assertEquals("[🛵 機車]", HudFormatters.formatVehicleTag(VehicleType.SCOOTER))
        assertEquals("[🏍️ 重機]", HudFormatters.formatVehicleTag(VehicleType.HEAVY_MOTORCYCLE))
    }

    @Test
    fun `isOverspeed detects speed limit violations accurately`() {
        assertTrue(HudFormatters.isOverspeed(61, 60))
        assertTrue(HudFormatters.isOverspeed(110, 100))
        assertFalse(HudFormatters.isOverspeed(60, 60))
        assertFalse(HudFormatters.isOverspeed(55, 60))
        assertFalse(HudFormatters.isOverspeed(70, null))
        assertFalse(HudFormatters.isOverspeed(70, 0))
    }

    @Test
    fun `formatGpsStatusLabel returns correct localized labels for all states`() {
        assertEquals("GPS: 未連接", HudFormatters.formatGpsStatusLabel(app.garminlike.nav.ui.hud.model.GpsStatus.UNKNOWN))
        assertEquals("GPS: 搜尋中...", HudFormatters.formatGpsStatusLabel(app.garminlike.nav.ui.hud.model.GpsStatus.SEARCHING))
        assertEquals("GPS: 良好", HudFormatters.formatGpsStatusLabel(app.garminlike.nav.ui.hud.model.GpsStatus.VALID))
        assertEquals("GPS: 訊號中斷", HudFormatters.formatGpsStatusLabel(app.garminlike.nav.ui.hud.model.GpsStatus.LOST))
    }
}
