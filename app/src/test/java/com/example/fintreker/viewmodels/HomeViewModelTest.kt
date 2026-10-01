package com.example.fintreker.viewmodels

import com.example.fintreker.MainDispatcherRule
import com.example.fintreker.collectInBackground
import com.example.fintreker.data.local.ExpenseEntity
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.BudgetStatus
import com.example.fintreker.domain.monthRange
import com.example.fintreker.ui.viewmodels.HomeUiState
import com.example.fintreker.ui.viewmodels.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val now = 1_790_000_000_000L
    private val repository = mockk<ExpenseRepository>()
    private val budget = MutableStateFlow(5000.0)
    private val secureStore = mockk<SecureStore> { every { budgetLimit } returns budget }

    private val recent = listOf(
        ExpenseEntity(id = 2, amount = 300.0, category = "FOOD", timestamp = now - 1_000),
        ExpenseEntity(id = 1, amount = 700.0, category = "HOUSING", timestamp = now - 2_000)
    )

    private fun stubRepository(spent: Double = 1000.0) {
        every { repository.observeMonthTotal(any()) } returns flowOf(spent)
        every { repository.observeExpenses(null, HomeViewModel.RECENT_COUNT) } returns flowOf(recent)
    }

    private fun viewModel() = HomeViewModel(repository, secureStore) { now }

    @Test
    fun `state is Loading until the flows are collected`() = runTest {
        stubRepository()

        val vm = viewModel()

        assertEquals(HomeUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `emits Success with balance computed from budget and month total`() = runTest {
        stubRepository(spent = 1000.0)
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(4000.0, state.balance.remaining, 0.0)
        assertEquals(BudgetStatus.OK, state.balance.status)
        assertEquals(recent, state.recent)
    }

    @Test
    fun `queries the total for the current calendar month`() = runTest {
        stubRepository()
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        verify { repository.observeMonthTotal(monthRange(now)) }
    }

    @Test
    fun `balance reacts to a changed budget limit`() = runTest {
        stubRepository(spent = 1000.0)
        val vm = viewModel()
        collectInBackground(vm.uiState)
        advanceUntilIdle()

        budget.value = 900.0
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(BudgetStatus.EXCEEDED, state.balance.status)
        assertEquals(-100.0, state.balance.remaining, 0.0)
    }

    @Test
    fun `without a limit the status is NO_LIMIT`() = runTest {
        stubRepository(spent = 250.0)
        budget.value = 0.0
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertEquals(BudgetStatus.NO_LIMIT, state.balance.status)
        assertEquals(250.0, state.balance.spent, 0.0)
    }

    @Test
    fun `database failure becomes an Error state instead of a crash`() = runTest {
        every { repository.observeMonthTotal(any()) } returns flow { throw IllegalStateException("db is locked") }
        every { repository.observeExpenses(null, HomeViewModel.RECENT_COUNT) } returns flowOf(recent)
        val vm = viewModel()

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("db is locked", (state as HomeUiState.Error).message)
    }
}
