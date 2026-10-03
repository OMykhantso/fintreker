package com.example.fintreker.domain

import java.time.Instant
import java.time.ZoneId

/** Півінтервал часу в мілісекундах: `[startMillis, endMillis)`. */
data class TimeRange(val startMillis: Long, val endMillis: Long)

/** Межі календарного місяця, в який потрапляє [nowMillis]. */
fun monthRange(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): TimeRange {
    val firstDay = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate().withDayOfMonth(1)
    val start = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = firstDay.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return TimeRange(start, end)
}
