package com.example.fintreker.data

import com.example.fintreker.data.local.ExpenseDao
import com.example.fintreker.data.local.ExpenseEntity
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.domain.Category
import com.example.fintreker.domain.CategoryTotal
import com.example.fintreker.domain.TimeRange
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseRepositoryTest {

    private val dao = mockk<ExpenseDao>()
    private val repository = ExpenseRepository(dao)
    private val range = TimeRange(1_000L, 2_000L)

    @Test
    fun `add maps the category to its enum name and keeps all fields`() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        repository.add(amount = 25.5, category = Category.HEALTH, note = "Аптека", timestamp = 123L)

        coVerify(exactly = 1) {
            dao.insert(
                ExpenseEntity(amount = 25.5, category = "HEALTH", timestamp = 123L, note = "Аптека")
            )
        }
    }

    @Test
    fun `month total passes the range bounds to the dao and returns the first value`() = runTest {
        every { dao.observeTotal(1_000L, 2_000L) } returns flowOf(420.0, 500.0)

        assertEquals(420.0, repository.monthTotal(range), 0.0)
    }

    @Test
    fun `category totals are delegated to the aggregation query`() = runTest {
        val totals = listOf(CategoryTotal("FOOD", 300.0), CategoryTotal("HOUSING", 1200.0))
        every { dao.observeCategoryTotals(1_000L, 2_000L) } returns flowOf(totals)

        assertEquals(totals, repository.observeCategoryTotals(range).first())
    }

    @Test
    fun `expense list passes category filter and limit`() = runTest {
        val items = listOf(ExpenseEntity(id = 1, amount = 5.0, category = "FOOD", timestamp = 1L))
        every { dao.observeExpenses("FOOD", 40) } returns flowOf(items)

        assertEquals(items, repository.observeExpenses("FOOD", 40).first())
    }

    @Test
    fun `delete and clear are delegated`() = runTest {
        coEvery { dao.deleteById(any()) } returns Unit
        coEvery { dao.clear() } returns Unit

        repository.delete(9L)
        repository.clear()

        coVerify(exactly = 1) { dao.deleteById(9L) }
        coVerify(exactly = 1) { dao.clear() }
    }

    @Test
    fun `addAll inserts the whole batch`() = runTest {
        val batch = listOf(
            ExpenseEntity(amount = 1.0, category = "FOOD", timestamp = 1L),
            ExpenseEntity(amount = 2.0, category = "OTHER", timestamp = 2L)
        )
        coEvery { dao.insertAll(any()) } returns Unit

        repository.addAll(batch)

        coVerify(exactly = 1) { dao.insertAll(batch) }
    }
}
