package com.example.fintreker.viewmodels

import com.example.fintreker.MainDispatcherRule
import com.example.fintreker.collectInBackground
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.domain.Category
import com.example.fintreker.domain.CategoryTotal
import com.example.fintreker.domain.monthRange
import com.example.fintreker.ui.viewmodels.AnalyticsUiState
import com.example.fintreker.ui.viewmodels.AnalyticsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val now = 1_790_000_000_000L
    private val repository = mockk<ExpenseRepository>()

    private fun viewModel() = AnalyticsViewModel(repository) { now }

    @Test
    fun `starts in Loading`() = runTest {
        every { repository.observeCategoryTotals(any()) } returns flowOf(emptyList())

        assertEquals(AnalyticsUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun `aggregated totals become shares for the pie chart`() = runTest {
        every { repository.observeCategoryTotals(any()) } returns flowOf(
            listOf(CategoryTotal("FOOD", 300.0), CategoryTotal("TRANSPORT", 100.0))
        )
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        val state = vm.uiState.value as AnalyticsUiState.Success
        assertEquals(400.0, state.total, 0.0)
        assertEquals(listOf(Category.FOOD, Category.TRANSPORT), state.shares.map { it.category })
        assertEquals(0.75f, state.shares.first().fraction, 1e-6f)
    }

    @Test
    fun `requests totals for the current month`() = runTest {
        every { repository.observeCategoryTotals(any()) } returns flowOf(emptyList())
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        verify { repository.observeCategoryTotals(monthRange(now)) }
    }

    @Test
    fun `no expenses gives Empty`() = runTest {
        every { repository.observeCategoryTotals(any()) } returns flowOf(emptyList())
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        assertEquals(AnalyticsUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `only zero totals also give Empty`() = runTest {
        every { repository.observeCategoryTotals(any()) } returns flowOf(listOf(CategoryTotal("FOOD", 0.0)))
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        assertEquals(AnalyticsUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `database failure gives Error`() = runTest {
        every { repository.observeCategoryTotals(any()) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        assertEquals(AnalyticsUiState.Error("boom"), vm.uiState.value)
    }
}
