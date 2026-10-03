package com.example.fintreker.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val UKRAINIAN = Locale.forLanguageTag("uk-UA")

/** `12 345,50 ₴` -> розділювач тисяч — нерозривний пробіл, щоб число не переносилось. */
fun formatAmount(value: Double, symbol: String = CurrencyCode.UAH.symbol): String {
    val formatted = String.format(Locale.US, "%,.2f", value).replace(',', ' ')
    return "$formatted $symbol"
}

fun formatPercent(fraction: Double): String = "${Math.round(fraction * 100.0)}%"

fun formatDateTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofPattern("d MMM, HH:mm", UKRAINIAN)
        .format(Instant.ofEpochMilli(millis).atZone(zone))

/** Заголовок місяця: «жовтень 2026». */
fun formatMonthTitle(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofPattern("LLLL yyyy", UKRAINIAN)
        .format(Instant.ofEpochMilli(millis).atZone(zone))
