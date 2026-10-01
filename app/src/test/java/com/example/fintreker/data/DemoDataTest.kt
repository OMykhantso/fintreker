package com.example.fintreker.data

import com.example.fintreker.domain.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataTest {

    private val now = 1_790_000_000_000L

    @Test
    fun `generates the requested number of expenses`() {
        assertEquals(45, DemoData.generate(now).size)
        assertEquals(3, DemoData.generate(now, count = 3).size)
        assertEquals(0, DemoData.generate(now, count = 0).size)
    }

    @Test
    fun `same seed gives the same data`() {
        assertEquals(DemoData.generate(now, seed = 1), DemoData.generate(now, seed = 1))
    }

    @Test
    fun `amounts are positive and categories are known`() {
        DemoData.generate(now).forEach { expense ->
            assertTrue(expense.amount > 0.0)
            assertTrue(Category.fromName(expense.category) != null)
        }
    }

    @Test
    fun `expenses are in the past and ordered from newest to oldest`() {
        val timestamps = DemoData.generate(now).map { it.timestamp }

        assertTrue(timestamps.all { it <= now })
        assertEquals(timestamps.sortedDescending(), timestamps)
    }

    @Test
    fun `is large enough for three pages of twenty`() {
        assertTrue(DemoData.generate(now).size > 40)
    }
}
