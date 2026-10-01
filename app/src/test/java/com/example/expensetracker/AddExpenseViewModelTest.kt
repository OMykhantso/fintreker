package com.example.expensetracker

import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.security.SecureStorage
import com.example.expensetracker.domain.Category
import com.example.expensetracker.ui.viewmodels.AddExpenseViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<ExpenseRepository>(relaxed = true)
    private val secure = mockk<SecureStorage>()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeMonthTotal() } returns flowOf(900.0)
        every { secure.budgetLimit } returns MutableStateFlow(1000.0)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `save with empty amount shows error and does not call repository`() = runTest(dispatcher) {
        val vm = AddExpenseViewModel(repository, secure)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.save()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.form.error)
        coVerify(exactly = 0) { repository.add(any(), any(), any()) }
    }

    @Test fun `save with valid amount stores expense and marks saved`() = runTest(dispatcher) {
        val vm = AddExpenseViewModel(repository, secure)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onAmountChange("50,5")
        vm.onCategoryChange(Category.TRANSPORT)
        vm.onNoteChange("Метро")
        vm.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.add(50.5, "TRANSPORT", "Метро") }
        assertTrue(vm.uiState.value.form.saved)
    }

    @Test fun `invalid characters are ignored in amount input`() = runTest(dispatcher) {
        val vm = AddExpenseViewModel(repository, secure)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onAmountChange("12")
        vm.onAmountChange("12-")
        advanceUntilIdle()

        assertEquals("12", vm.uiState.value.form.amount)
    }

    @Test fun `warns when expense exceeds remaining budget`() = runTest(dispatcher) {
        val vm = AddExpenseViewModel(repository, secure)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onAmountChange("50")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.willExceedBudget) // 900 + 50 <= 1000

        vm.onAmountChange("150")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.willExceedBudget)
        assertEquals(100.0, vm.uiState.value.remaining, 0.0)
    }
}
