package com.example.fintreker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class TimeRulesTest {

    private val kyiv = ZoneId.of("Europe/Kyiv")

    private fun millis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): Long =
        ZonedDateTime.of(year, month, day, hour, minute, second, 0, kyiv).toInstant().toEpochMilli()

    // ---------------------------------------------------------------- monthRange

    @Test
    fun `month range covers the calendar month of the given moment`() {
        val range = monthRange(millis(2026, 10, 15, 12, 0), kyiv)

        assertEquals(millis(2026, 10, 1, 0, 0), range.startMillis)
        assertEquals(millis(2026, 11, 1, 0, 0), range.endMillis)
    }

    @Test
    fun `month range at the very start of a month starts exactly there`() {
        val start = millis(2026, 3, 1, 0, 0)
        val range = monthRange(start, kyiv)

        assertEquals(start, range.startMillis)
        assertEquals(millis(2026, 4, 1, 0, 0), range.endMillis)
    }

    @Test
    fun `last millisecond of a month still belongs to that month`() {
        val lastMoment = millis(2026, 4, 1, 0, 0) - 1
        val range = monthRange(lastMoment, kyiv)

        assertEquals(millis(2026, 3, 1, 0, 0), range.startMillis)
        assertTrue(lastMoment < range.endMillis)
    }

    @Test
    fun `december rolls over to january of the next year`() {
        val range = monthRange(millis(2026, 12, 31, 23, 59), kyiv)

        assertEquals(millis(2026, 12, 1, 0, 0), range.startMillis)
        assertEquals(millis(2027, 1, 1, 0, 0), range.endMillis)
    }

    @Test
    fun `leap february has 29 days`() {
        val range = monthRange(millis(2028, 2, 10, 9, 0), kyiv)
        val days = TimeUnit.MILLISECONDS.toDays(range.endMillis - range.startMillis)

        assertEquals(29L, days)
    }

    // ---------------------------------------------------------------- millisUntilNext

    @Test
    fun `before reminder time the delay is until today`() {
        val now = millis(2026, 6, 10, 10, 0)
        assertEquals(TimeUnit.HOURS.toMillis(10), millisUntilNext(20, 0, now, kyiv))
    }

    @Test
    fun `after reminder time the delay is until tomorrow`() {
        val now = millis(2026, 6, 10, 21, 0)
        assertEquals(TimeUnit.HOURS.toMillis(23), millisUntilNext(20, 0, now, kyiv))
    }

    @Test
    fun `exactly at reminder time the next one is in 24 hours`() {
        val now = millis(2026, 6, 10, 20, 0)
        assertEquals(TimeUnit.HOURS.toMillis(24), millisUntilNext(20, 0, now, kyiv))
    }

    @Test
    fun `one second before reminder time waits one second`() {
        val now = millis(2026, 6, 10, 19, 59, 59)
        assertEquals(1_000L, millisUntilNext(20, 0, now, kyiv))
    }

    @Test
    fun `delay is correct across the end of daylight saving time`() {
        // У Києві 25.10.2026 о 04:00 годинник переводять на годину назад: між 21:00 24.10
        // і 20:00 25.10 минає 24 реальні години, а не 23.
        val now = millis(2026, 10, 24, 21, 0)
        assertEquals(TimeUnit.HOURS.toMillis(24), millisUntilNext(20, 0, now, kyiv))
    }

    @Test
    fun `delay is correct across the start of daylight saving time`() {
        // 29.03.2026 о 03:00 годинник переводять на годину вперед: між 21:00 28.03 і 20:00 29.03 — 22 години.
        val now = millis(2026, 3, 28, 21, 0)
        assertEquals(TimeUnit.HOURS.toMillis(22), millisUntilNext(20, 0, now, kyiv))
    }
}
