package com.example.fintreker.viewmodels

import com.example.fintreker.MainDispatcherRule
import com.example.fintreker.collectInBackground
import com.example.fintreker.data.local.ExpenseEntity
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.domain.Category
import com.example.fintreker.ui.viewmodels.HistoryUiState
import com.example.fintreker.ui.viewmodels.HistoryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Пагінація (Infinite Scroll) та фільтр за категорією. */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = mockk<ExpenseRepository>()

    /** 55 витрат, найновіші першими; парні — FOOD, непарні — HEALTH. */
    private val all = List(55) { index ->
        ExpenseEntity(
            id = (index + 1).toLong(),
            amount = 10.0 + index,
            category = if (index % 2 == 0) "FOOD" else "HEALTH",
            timestamp = 1_000_000L - index
        )
    }

    @Before
    fun setUp() {
        every { repository.observeExpenses(any(), any()) } answers {
            val category = firstArg<String?>()
            val limit = secondArg<Int>()
            flowOf(all.filter { category == null || it.category == category }.take(limit))
        }
    }

    private fun viewModel() = HistoryViewModel(repository, pageSize = 20)

    private fun HistoryViewModel.success() = uiState.value as HistoryUiState.Success

    @Test
    fun `first page has page size items and reports more`() = runTest {
        val vm = viewModel()
        collectInBackground(vm.uiState)
        advanceUntilIdle()

        assertEquals(20, vm.success().items.size)
        assertTrue(vm.success().hasMore)
        assertEquals(null, vm.success().category)
    }

    @Test
    fun `loadMore appends the next pages until everything is loaded`() = runTest {
        val vm = viewModel()
        collectInBackground(vm.uiState)
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()
        assertEquals(40, vm.success().items.size)
        assertTrue(vm.success().hasMore)

        vm.loadMore()
        advanceUntilIdle()
        assertEquals(55, vm.success().items.size)
        assertFalse(vm.success().hasMore)
    }

    @Test
    fun `loadMore after the last page does nothing`() = runTest {
        val vm = viewModel()
        collectInBackground(vm.uiState)
        advanceUntilIdle()
        repeat(2) {
            vm.loadMore()
            advanceUntilIdle()
        }

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(55, vm.success().items.size)
        verify(exactly = 0) { repository.observeExpenses(any(), 80) }
    }

    @Test
    fun `repeated loadMore before data arrives requests only one extra page`() = runTest {
        val vm = viewModel()
        collectInBackground(vm.uiState)
        advanceUntilIdle()

        vm.loadMore()
        vm.loadMore()
        vm.loadMore()
        advanceUntilIdle()

        assertEquals(40, vm.success().items.size)
        verify(exactly = 0) { repository.observeExpenses(any(), 60) }
    }

    @Test
    fun `loadMore before the first page is loaded is ignored`() = runTest {
        val vm = viewModel()

        vm.loadMore()
        collectInBackground(vm.uiState)
        advanceUntilIdle()

        assertEquals(20, vm.success().items.size)
    }

    @Test
    fun `selecting a category filters the list and resets pagination`() = runTest {
        val vm = viewModel()
        collectInBackground(vm.uiState)
        advanceUntilIdle()
        vm.loadMore()
        advanceUntilIdle()
        assertEquals(40, vm.success().items.size)

        vm.setCategory("HEALTH")
        advanceUntilIdle()

        val state = vm.success()
        assertEquals(20, state.items.size)
        assertTrue(state.items.all { it.category == "HEALTH" })
        assertEquals(Category.HEALTH, state.category)
        assertTrue(state.hasMore) // у БД 27 записів HEALTH
    }

    @Test
    fun `filtered list that fits one page has no more`() = runTest {
        every { repository.observeExpenses("HOUSING", any()) } returns flowOf(all.take(3))
        val vm = viewModel()
        collectInBackground(vm.uiState)

        vm.setCategory("HOUSING")
        advanceUntilIdle()

        assertEquals(3, vm.success().items.size)
        assertFalse(vm.success().hasMore)
    }

    @Test
    fun `selecting the same category again keeps loaded pages`() = runTest {
        val vm = viewModel()
        collectInBackground(vm.uiState)
        vm.setCategory("FOOD")
        advanceUntilIdle()
        vm.loadMore()
        advanceUntilIdle()
        assertEquals(28, vm.success().items.size) // у БД 28 записів FOOD, обидві сторінки

        vm.setCategory("FOOD")
        advanceUntilIdle()

        assertEquals(28, vm.success().items.size)
    }

    @Test
    fun `clearing the filter returns to all categories`() = runTest {
        val vm = viewModel()
        collectInBackground(vm.uiState)
        vm.setCategory("HEALTH")
        advanceUntilIdle()

        vm.setCategory(null)
        advanceUntilIdle()

        assertEquals(null, vm.success().category)
        assertTrue(vm.success().items.any { it.category == "FOOD" })
    }

    @Test
    fun `delete is forwarded to the repository`() = runTest {
        coEvery { repository.delete(any()) } returns Unit
        val vm = viewModel()

        vm.delete(7L)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.delete(7L) }
    }
}
