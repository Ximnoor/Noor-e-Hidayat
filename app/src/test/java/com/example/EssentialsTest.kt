package com.example

import com.example.essentials.*
import com.example.quran.SurahData
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.json.JSONObject

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EssentialsTest {
    @Test fun qiblaKnownCities() {
        assertEquals(118.99,QiblaMath.bearing(51.5074,-0.1278),0.2)
        assertEquals(58.48,QiblaMath.bearing(40.7128,-74.0060),0.2)
        assertEquals(277.50,QiblaMath.bearing(33.6844,73.0479),0.8)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidCoordinates() { QiblaMath.bearing(Double.NaN,0.0) }
    @Test fun quranIndexComplete() {
        assertEquals(114,SurahData.surahs.size)
        assertEquals(6236,SurahData.surahs.sumOf { it.ayahs })
    }
    private fun response(time: String): JSONObject = JSONObject().put("meta",JSONObject().put("timezone","Europe/London"))
        .put("date",JSONObject().put("readable","25 Oct 2026"))
        .put("timings",JSONObject().apply { WorldPrayer.names.forEach { put(it,time) } })
    @Test fun honorsDaylightSavingOffsetNotDeviceTimezone() {
        val first=WorldPrayer.parse(response("2026-10-25T01:30:00+01:00"))
        val second=WorldPrayer.parse(response("2026-10-25T01:30:00+00:00"))
        assertEquals(3600000L,second.alarms.first().second-first.alarms.first().second)
        assertEquals(5,first.alarms.size)
        assertFalse(first.alarms.any { it.first=="Sunrise" })
    }
    @Test fun unavailablePrayerCannotCreateAlarm() {
        assertTrue(WorldPrayer.parse(response("-----")).alarms.isEmpty())
    }
    @Test fun duasHaveUrduAndTraceableSources() {
        assertTrue(DailyDuas.all.size>=10)
        DailyDuas.all.forEach { assertTrue(it.arabic.isNotBlank()); assertTrue(it.urdu.any { char -> char in '\u0600'..'\u06ff' }); assertTrue(it.source.startsWith("https://")) }
    }
}
