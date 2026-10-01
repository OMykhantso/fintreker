package com.example.fintreker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsAndFormattingTest {

    // ---------------------------------------------------------------- calculateShares

    @Test
    fun `shares are fractions of the total sorted descending`() {
        val shares = calculateShares(
            listOf(
                CategoryTotal("TRANSPORT", 100.0),
                CategoryTotal("FOOD", 300.0)
            )
        )

        assertEquals(listOf(Category.FOOD, Category.TRANSPORT), shares.map { it.category })
        assertEquals(0.75f, shares[0].fraction, 1e-6f)
        assertEquals(0.25f, shares[1].fraction, 1e-6f)
        assertEquals(1.0f, shares.sumOf { it.fraction.toDouble() }.toFloat(), 1e-5f)
    }

    @Test
    fun `unknown categories are merged into OTHER`() {
        val shares = calculateShares(
            listOf(
                CategoryTotal("XYZ", 50.0),
                CategoryTotal("OTHER", 50.0),
                CategoryTotal("FOOD", 100.0)
            )
        )

        val other = shares.first { it.category == Category.OTHER }
        assertEquals(100.0, other.total, 0.0)
        assertEquals(2, shares.size)
    }

    @Test
    fun `zero, negative and invalid totals are ignored`() {
        val shares = calculateShares(
            listOf(
                CategoryTotal("FOOD", 0.0),
                CategoryTotal("HEALTH", -10.0),
                CategoryTotal("HOUSING", Double.NaN),
                CategoryTotal("TRANSPORT", 40.0)
            )
        )

        assertEquals(1, shares.size)
        assertEquals(1.0f, shares.single().fraction, 0f)
    }

    @Test
    fun `no meaningful data gives empty shares`() {
        assertTrue(calculateShares(emptyList()).isEmpty())
        assertTrue(calculateShares(listOf(CategoryTotal("FOOD", 0.0))).isEmpty())
    }

    // ---------------------------------------------------------------- Category

    @Test
    fun `category lookup is safe for unknown and null names`() {
        assertEquals(Category.HEALTH, Category.fromName("HEALTH"))
        assertEquals(null, Category.fromName("health"))
        assertEquals(null, Category.fromName(null))
        assertEquals(Category.OTHER, Category.fromNameOrOther("???"))
    }

    @Test
    fun `every category has a unique opaque color`() {
        val colors = Category.entries.map { it.colorArgb }
        assertEquals(colors.size, colors.toSet().size)
        colors.forEach { assertEquals("alpha must be FF", 0xFFL, it ushr 24) }
    }

    // ---------------------------------------------------------------- formatting

    @Test
    fun `formats amounts with grouping and currency symbol`() {
        assertEquals("1 234.50 ₴", formatAmount(1234.5))
        assertEquals("0.00 ₴", formatAmount(0.0))
        assertEquals("-50.00 $", formatAmount(-50.0, "$"))
        assertEquals("1 000 000.00 €", formatAmount(1_000_000.0, "€"))
    }

    @Test
    fun `formats percent rounded to integer`() {
        assertEquals("26%", formatPercent(0.256))
        assertEquals("0%", formatPercent(0.0))
        assertEquals("120%", formatPercent(1.2))
    }
}
