package com.example.fintreker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Валідація суми/ліміту, баланс і конвертація валют — включно з edge cases. */
class MoneyTest {

    private fun valid(input: String): Double =
        (validateAmount(input) as AmountValidation.Valid).amount

    private fun errorOf(input: String): AmountError =
        (validateAmount(input) as AmountValidation.Invalid).error

    // ---------------------------------------------------------------- validateAmount

    @Test
    fun `accepts integers and decimals with dot or comma`() {
        assertEquals(100.0, valid("100"), 0.0)
        assertEquals(12.5, valid("12.5"), 0.0)
        assertEquals(12.5, valid("12,50"), 0.0)
        assertEquals(5.0, valid("5."), 0.0)
        assertEquals(0.5, valid(".5"), 0.0)
    }

    @Test
    fun `ignores surrounding and inner spaces`() {
        assertEquals(1250.5, valid(" 1 250,5 "), 0.0)
    }

    @Test
    fun `rounds to kopecks`() {
        assertEquals(10.24, valid("10.239"), 0.0)
        assertEquals(10.23, valid("10.234"), 0.0)
    }

    @Test
    fun `empty or blank input is EMPTY`() {
        assertEquals(AmountError.EMPTY, errorOf(""))
        assertEquals(AmountError.EMPTY, errorOf("   "))
    }

    @Test
    fun `non numeric input is NOT_A_NUMBER`() {
        listOf("abc", "12abc", "1e3", "5d", "5f", "NaN", "Infinity", "0x10", "1.2.3", "--5", "+5", "5-")
            .forEach { input ->
                assertEquals("input=$input", AmountError.NOT_A_NUMBER, errorOf(input))
            }
    }

    @Test
    fun `negative amounts are NOT_POSITIVE`() {
        listOf("-5", "-0.5", "-0", "-100,25").forEach { input ->
            assertEquals("input=$input", AmountError.NOT_POSITIVE, errorOf(input))
        }
    }

    @Test
    fun `zero and amounts that round to zero are NOT_POSITIVE`() {
        listOf("0", "0.00", "0,0", "0.004").forEach { input ->
            assertEquals("input=$input", AmountError.NOT_POSITIVE, errorOf(input))
        }
    }

    @Test
    fun `smallest representable amount is accepted`() {
        assertEquals(0.01, valid("0.01"), 0.0)
    }

    @Test
    fun `amount above maximum is TOO_LARGE but the maximum itself is valid`() {
        assertEquals(MAX_AMOUNT, valid("1000000000"), 0.0)
        assertEquals(AmountError.TOO_LARGE, errorOf("1000000000.01"))
        assertEquals(AmountError.TOO_LARGE, errorOf("99999999999999999999999999999"))
    }

    // ---------------------------------------------------------------- budget limit

    @Test
    fun `budget limit must be positive`() {
        assertEquals(5000.0, (validateBudgetLimit("5000") as AmountValidation.Valid).amount, 0.0)
        assertEquals(AmountError.NOT_POSITIVE, (validateBudgetLimit("0") as AmountValidation.Invalid).error)
        assertEquals(AmountError.NOT_POSITIVE, (validateBudgetLimit("-100") as AmountValidation.Invalid).error)
        assertEquals(AmountError.EMPTY, (validateBudgetLimit("") as AmountValidation.Invalid).error)
    }

    // ---------------------------------------------------------------- calculateBalance

    @Test
    fun `balance below warning threshold is OK`() {
        val balance = calculateBalance(budget = 5000.0, spent = 1000.0)
        assertEquals(BudgetStatus.OK, balance.status)
        assertEquals(4000.0, balance.remaining, 0.0)
        assertEquals(0.2, balance.usedFraction, 1e-9)
    }

    @Test
    fun `exactly eighty percent used is WARNING`() {
        assertEquals(BudgetStatus.WARNING, calculateBalance(5000.0, 4000.0).status)
    }

    @Test
    fun `just below eighty percent is still OK`() {
        assertEquals(BudgetStatus.OK, calculateBalance(5000.0, 3999.99).status)
    }

    @Test
    fun `spending exactly the whole limit is WARNING with zero remaining`() {
        val balance = calculateBalance(5000.0, 5000.0)
        assertEquals(BudgetStatus.WARNING, balance.status)
        assertEquals(0.0, balance.remaining, 0.0)
    }

    @Test
    fun `exceeding the limit is EXCEEDED with negative remaining`() {
        val balance = calculateBalance(5000.0, 5500.0)
        assertEquals(BudgetStatus.EXCEEDED, balance.status)
        assertEquals(-500.0, balance.remaining, 0.0)
        assertTrue(balance.usedFraction > 1.0)
    }

    @Test
    fun `zero or invalid budget means NO_LIMIT`() {
        listOf(0.0, -100.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { budget ->
            val balance = calculateBalance(budget, 250.0)
            assertEquals("budget=$budget", BudgetStatus.NO_LIMIT, balance.status)
            assertEquals(250.0, balance.spent, 0.0)
            assertEquals(-250.0, balance.remaining, 0.0)
            assertEquals(0.0, balance.usedFraction, 0.0)
        }
    }

    @Test
    fun `negative or NaN spent is treated as zero`() {
        listOf(-50.0, Double.NaN).forEach { spent ->
            val balance = calculateBalance(1000.0, spent)
            assertEquals("spent=$spent", 0.0, balance.spent, 0.0)
            assertEquals("spent=$spent", 1000.0, balance.remaining, 0.0)
            assertEquals("spent=$spent", BudgetStatus.OK, balance.status)
        }
    }

    @Test
    fun `remaining is rounded to kopecks`() {
        assertEquals(99.9, calculateBalance(100.10, 0.2).remaining, 0.0)
    }

    @Test
    fun `no limit and no expenses gives positive zero remaining`() {
        val balance = calculateBalance(0.0, 0.0)
        assertEquals("0.0", balance.remaining.toString())
    }

    // ---------------------------------------------------------------- convertFromUah

    @Test
    fun `converts hryvnia to foreign currency by NBU rate`() {
        assertEquals(100.0, convertFromUah(4100.0, 41.0)!!, 0.0)
        assertEquals(2.42, convertFromUah(100.0, 41.3)!!, 0.0)
    }

    @Test
    fun `converts negative balance as well`() {
        assertEquals(-10.0, convertFromUah(-410.0, 41.0)!!, 0.0)
    }

    @Test
    fun `zero, negative, NaN or missing rate gives null instead of a wrong number`() {
        listOf(0.0, -41.0, Double.NaN, Double.POSITIVE_INFINITY, null).forEach { rate ->
            assertNull("rate=$rate", convertFromUah(1000.0, rate))
        }
    }

    @Test
    fun `non finite amount gives null`() {
        assertNull(convertFromUah(Double.NaN, 41.0))
        assertNull(convertFromUah(Double.POSITIVE_INFINITY, 41.0))
    }

    @Test
    fun `zero amount converts to zero`() {
        assertEquals(0.0, convertFromUah(0.0, 41.0)!!, 0.0)
    }
}
