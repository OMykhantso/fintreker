package com.example.fintreker.viewmodels

import com.example.fintreker.MainDispatcherRule
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.AmountError
import com.example.fintreker.domain.Category
import com.example.fintreker.domain.monthRange
import com.example.fintreker.ui.viewmodels.AddExpenseViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val now = 1_790_000_000_000L
    private val repository = mockk<ExpenseRepository>()
    private val budget = MutableStateFlow(0.0)
    private val secureStore = mockk<SecureStore> { every { budgetLimit } returns budget }

    @Before
    fun setUp() {
        coEvery { repository.add(any(), any(), any(), any()) } returns Unit
        coEvery { repository.monthTotal(any()) } returns 0.0
    }

    private fun viewModel() = AddExpenseViewModel(repository, secureStore) { now }

    @Test
    fun `empty amount shows error and does not touch the repository`() = runTest {
        val vm = viewModel()

        vm.save()
        advanceUntilIdle()

        assertEquals(AmountError.EMPTY, vm.uiState.value.amountError)
        assertFalse(vm.uiState.value.saved)
        coVerify(exactly = 0) { repository.add(any(), any(), any(), any()) }
    }

    @Test
    fun `zero amount is rejected`() = runTest {
        val vm = viewModel()
        vm.onAmountChange("0")

        vm.save()
        advanceUntilIdle()

        assertEquals(AmountError.NOT_POSITIVE, vm.uiState.value.amountError)
        coVerify(exactly = 0) { repository.add(any(), any(), any(), any()) }
    }

    @Test
    fun `negative amount cannot even be typed - minus sign is filtered out`() = runTest {
        val vm = viewModel()

        vm.onAmountChange("-50")

        assertEquals("50", vm.uiState.value.amountText)
    }

    @Test
    fun `valid amount is saved with category note and timestamp`() = runTest {
        val vm = viewModel()
        vm.onAmountChange("25,5")
        vm.onCategoryChange(Category.TRANSPORT)
        vm.onNoteChange("  Метро  ")

        vm.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.add(25.5, Category.TRANSPORT, "Метро", now) }
        assertTrue(vm.uiState.value.saved)
        assertFalse(vm.uiState.value.isSaving)
        assertNull(vm.uiState.value.amountError)
    }

    @Test
    fun `saving checks the budget for the current month`() = runTest {
        val vm = viewModel()
        vm.onAmountChange("10")

        vm.save()
        advanceUntilIdle()

        coVerify { repository.monthTotal(monthRange(now)) }
    }

    @Test
    fun `limit exceeded flag is raised when the month total passes the budget`() = runTest {
        budget.value = 1000.0
        coEvery { repository.monthTotal(any()) } returns 1500.0
        val vm = viewModel()
        vm.onAmountChange("500")

        vm.save()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saved)
        assertTrue(vm.uiState.value.limitExceeded)
    }

    @Test
    fun `limit exceeded flag is not raised within the budget or without a budget`() = runTest {
        coEvery { repository.monthTotal(any()) } returns 1500.0

        budget.value = 5000.0
        val withinBudget = viewModel().apply { onAmountChange("10") }
        withinBudget.save()
        advanceUntilIdle()
        assertFalse(withinBudget.uiState.value.limitExceeded)

        budget.value = 0.0
        val noBudget = viewModel().apply { onAmountChange("10") }
        noBudget.save()
        advanceUntilIdle()
        assertFalse(noBudget.uiState.value.limitExceeded)
    }

    @Test
    fun `typing filters out letters and keeps digits and separators`() = runTest {
        val vm = viewModel()

        vm.onAmountChange("1a2,b3.4 ")

        assertEquals("12,3.4", vm.uiState.value.amountText)
    }

    @Test
    fun `typing clears a previous validation error`() = runTest {
        val vm = viewModel()
        vm.save()
        assertEquals(AmountError.EMPTY, vm.uiState.value.amountError)

        vm.onAmountChange("5")

        assertNull(vm.uiState.value.amountError)
    }

    @Test
    fun `note is limited in length`() = runTest {
        val vm = viewModel()

        vm.onNoteChange("x".repeat(500))

        assertEquals(AddExpenseViewModel.MAX_NOTE_LENGTH, vm.uiState.value.note.length)
    }

    @Test
    fun `double tap on save stores the expense only once`() = runTest {
        val vm = viewModel()
        vm.onAmountChange("100")

        vm.save()
        vm.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.add(any(), any(), any(), any()) }
    }

    @Test
    fun `database failure keeps the form and reports an error`() = runTest {
        coEvery { repository.add(any(), any(), any(), any()) } throws IOException("disk full")
        val vm = viewModel()
        vm.onAmountChange("100")

        vm.save()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.saved)
        assertFalse(state.isSaving)
        assertEquals("100", state.amountText)
        assertTrue(state.saveError != null)
    }

    @Test
    fun `handled save event resets the form`() = runTest {
        val vm = viewModel()
        vm.onAmountChange("100")
        vm.save()
        advanceUntilIdle()

        vm.onSavedHandled()

        assertEquals("", vm.uiState.value.amountText)
        assertFalse(vm.uiState.value.saved)
    }
}
