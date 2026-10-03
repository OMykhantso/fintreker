package com.example.fintreker.domain

import java.time.Duration
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

/** Година й хвилина щоденного нагадування (Лаб 5: щодня о 20:00). */
const val REMINDER_HOUR = 20
const val REMINDER_MINUTE = 0

/**
 * Скільки мілісекунд лишилось до найближчого `hour:minute`.
 * Якщо цей час сьогодні вже минув (або настає просто зараз) — рахуємо до завтрашнього.
 */
fun millisUntilNext(
    hour: Int,
    minute: Int,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): Long {
    val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
    var next = now.toLocalDate().atTime(hour, minute).atZone(zone)
    if (!next.isAfter(now)) {
        next = now.toLocalDate().plusDays(1).atTime(hour, minute).atZone(zone)
    }
    return Duration.between(now, next).toMillis()
}
