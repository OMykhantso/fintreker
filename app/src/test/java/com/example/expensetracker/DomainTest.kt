package com.example.expensetracker

import com.example.expensetracker.domain.AmountParser
import com.example.expensetracker.domain.BalanceCalculator
import com.example.expensetracker.domain.BudgetValidator
import com.example.expensetracker.domain.Currency
import com.example.expensetracker.domain.CurrencyConverter
import com.example.expensetracker.domain.MonthRange
import com.example.expensetracker.domain.ReminderTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class DomainTest {

    // --- AmountParser (валідація: сума > 0, edge cases)
    @Test fun `parses dot and comma decimals`() {
        assertEquals(12.5, AmountParser.parse("12.5")!!, 0.0)
        assertEquals(12.5, AmountParser.parse("12,5")!!, 0.0)
    }

    @Test fun `rejects zero, negative, blank and garbage`() {
        listOf("0", "0.00", "-5", "", "  ", "abc", "NaN", "Infinity", "1e12").forEach {
            assertNull("'$it' має бути відхилено", AmountParser.parse(it))
        }
    }

    @Test fun `input filter allows max two decimals`() {
        assertTrue(AmountParser.isValidInput("12.34"))
        assertTrue(AmountParser.isValidInput(""))
        assertFalse(AmountParser.isValidInput("12.345"))
        assertFalse(AmountParser.isValidInput("-1"))
        assertFalse(AmountParser.isValidInput("1.2.3"))
    }

    // --- BalanceCalculator
    @Test fun `remaining can be negative when budget exceeded`() {
        assertEquals(700.0, BalanceCalculator.remaining(1000.0, 300.0), 0.0)
        assertEquals(-50.0, BalanceCalculator.remaining(1000.0, 1050.0), 0.0)
    }

    @Test fun `progress is clamped and handles zero limit`() {
        assertEquals(0.5f, BalanceCalculator.progress(1000.0, 500.0), 0.0001f)
        assertEquals(1f, BalanceCalculator.progress(1000.0, 5000.0), 0f)
        assertEquals(0f, BalanceCalculator.progress(0.0, 0.0), 0f)
        assertEquals(1f, BalanceCalculator.progress(0.0, 10.0), 0f)
    }

    // --- BudgetValidator
    @Test fun `exceeds only when total goes over the limit`() {
        assertFalse(BudgetValidator.exceeds(1000.0, 900.0, 100.0))
        assertTrue(BudgetValidator.exceeds(1000.0, 900.0, 100.01))
        assertFalse(BudgetValidator.exceeds(0.0, 0.0, 5.0)) // ліміт не заданий
    }

    // --- CurrencyConverter (нульовий курс, відсутність мережі/кешу)
    @Test fun `converts uah to usd`() {
        assertEquals(10.0, CurrencyConverter.fromUah(400.0, Currency.USD, mapOf("USD" to 40.0))!!, 1e-9)
    }

    @Test fun `uah conversion is identity even without rates`() {
        assertEquals(123.0, CurrencyConverter.fromUah(123.0, Currency.UAH, emptyMap())!!, 0.0)
    }

    @Test fun `returns null for missing or zero rate`() {
        assertNull(CurrencyConverter.fromUah(100.0, Currency.EUR, emptyMap()))
        assertNull(CurrencyConverter.fromUah(100.0, Currency.USD, mapOf("USD" to 0.0)))
        assertNull(CurrencyConverter.fromUah(100.0, Currency.USD, mapOf("USD" to -1.0)))
    }

    // --- MonthRange
    @Test fun `month range covers whole month`() {
        val zone = ZoneOffset.UTC
        val now = LocalDateTime.of(2026, 2, 15, 12, 0).toInstant(zone).toEpochMilli()
        val r = MonthRange.of(now, ZoneId.of("UTC"))
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0).toInstant(zone).toEpochMilli(), r.start)
        assertEquals(LocalDateTime.of(2026, 3, 1, 0, 0).toInstant(zone).toEpochMilli(), r.end)
    }

    // --- ReminderTime (планування о 20:00)
    @Test fun `reminder is later today when time not passed`() {
        val now = LocalDateTime.of(2026, 10, 1, 18, 0)
        assertEquals(2 * 60 * 60 * 1000L, ReminderTime.millisUntilNext(now, 20, 0))
    }

    @Test fun `reminder moves to tomorrow when time passed`() {
        val now = LocalDateTime.of(2026, 10, 1, 21, 0)
        assertEquals(23 * 60 * 60 * 1000L, ReminderTime.millisUntilNext(now, 20, 0))
        val exact = LocalDateTime.of(2026, 10, 1, 20, 0)
        assertEquals(24 * 60 * 60 * 1000L, ReminderTime.millisUntilNext(exact, 20, 0))
    }
}
