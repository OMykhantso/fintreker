package com.example.fintreker.viewmodels

import com.example.fintreker.MainDispatcherRule
import com.example.fintreker.collectInBackground
import com.example.fintreker.data.local.ExpenseEntity
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.AmountError
import com.example.fintreker.domain.ReminderController
import com.example.fintreker.ui.viewmodels.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val now = 1_790_000_000_000L
    private val budget = MutableStateFlow(0.0)
    private val pinEnabled = MutableStateFlow(false)
    private val reminderEnabled = MutableStateFlow(true)

    private val secureStore = mockk<SecureStore>(relaxed = true) {
        every { budgetLimit } returns budget
        every { this@mockk.pinEnabled } returns this@SettingsViewModelTest.pinEnabled
        every { this@mockk.reminderEnabled } returns this@SettingsViewModelTest.reminderEnabled
    }
    private val repository = mockk<ExpenseRepository>()
    private val reminder = mockk<ReminderController>(relaxed = true)

    private lateinit var vm: SettingsViewModel

    @Before
    fun setUp() {
        vm = SettingsViewModel(secureStore, repository, reminder) { now }
    }

    @Test
    fun `state mirrors the secure store`() = runTest {
        budget.value = 7000.0
        pinEnabled.value = true
        reminderEnabled.value = false
        collectInBackground(vm.uiState)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(7000.0, state.budgetLimit, 0.0)
        assertTrue(state.pinEnabled)
        assertEquals(false, state.reminderEnabled)
    }

    @Test
    fun `valid budget is stored and confirmed`() = runTest {
        collectInBackground(vm.uiState)

        vm.saveBudget("5 000,50")
        advanceUntilIdle()

        verify(exactly = 1) { secureStore.setBudgetLimit(5000.5) }
        assertNull(vm.uiState.value.budgetError)
        assertEquals("Ліміт бюджету збережено", vm.uiState.value.message)
    }

    @Test
    fun `invalid budget is rejected without touching the store`() = runTest {
        collectInBackground(vm.uiState)

        vm.saveBudget("abc")
        advanceUntilIdle()
        assertEquals(AmountError.NOT_A_NUMBER, vm.uiState.value.budgetError)

        vm.saveBudget("0")
        advanceUntilIdle()
        assertEquals(AmountError.NOT_POSITIVE, vm.uiState.value.budgetError)

        vm.saveBudget("-500")
        advanceUntilIdle()
        assertEquals(AmountError.NOT_POSITIVE, vm.uiState.value.budgetError)

        verify(exactly = 0) { secureStore.setBudgetLimit(any()) }
    }

    @Test
    fun `successful save clears an earlier budget error`() = runTest {
        collectInBackground(vm.uiState)
        vm.saveBudget("abc")
        advanceUntilIdle()

        vm.saveBudget("1000")
        advanceUntilIdle()

        assertNull(vm.uiState.value.budgetError)
    }

    @Test
    fun `clearing the budget stores zero`() = runTest {
        vm.clearBudget()

        verify(exactly = 1) { secureStore.setBudgetLimit(0.0) }
    }

    @Test
    fun `four digit pin is stored`() = runTest {
        collectInBackground(vm.uiState)

        vm.setPin("1234")
        advanceUntilIdle()

        verify(exactly = 1) { secureStore.setPin("1234") }
        assertNull(vm.uiState.value.pinError)
    }

    @Test
    fun `malformed pin is rejected`() = runTest {
        collectInBackground(vm.uiState)

        listOf("", "12", "12345", "12ab").forEach { pin ->
            vm.setPin(pin)
            advanceUntilIdle()
            assertEquals("pin=$pin", "PIN має складатись із 4 цифр", vm.uiState.value.pinError)
        }

        verify(exactly = 0) { secureStore.setPin(any()) }
    }

    @Test
    fun `disabling pin clears the stored hash`() = runTest {
        vm.disablePin()

        verify(exactly = 1) { secureStore.clearPin() }
    }

    @Test
    fun `reminder toggle updates the store and the schedule`() = runTest {
        vm.setReminderEnabled(false)
        vm.setReminderEnabled(true)

        verify(exactly = 1) { secureStore.setReminderEnabled(false) }
        verify(exactly = 1) { secureStore.setReminderEnabled(true) }
        verify(exactly = 1) { reminder.cancelDaily() }
        verify(exactly = 1) { reminder.scheduleDaily() }
    }

    @Test
    fun `test reminder is sent immediately`() = runTest {
        vm.sendTestReminder()

        verify(exactly = 1) { reminder.sendTestNow() }
    }

    @Test
    fun `demo data fills enough rows to exercise pagination`() = runTest {
        val inserted = slot<List<ExpenseEntity>>()
        coEvery { repository.addAll(capture(inserted)) } just runs

        vm.addDemoData()
        advanceUntilIdle()

        assertTrue(inserted.captured.size > 40)
        assertTrue(inserted.captured.all { it.amount > 0.0 && it.timestamp <= now })
    }

    @Test
    fun `clearing expenses wipes the table`() = runTest {
        coEvery { repository.clear() } just runs

        vm.clearExpenses()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.clear() }
    }

    @Test
    fun `message is shown once and can be dismissed`() = runTest {
        collectInBackground(vm.uiState)
        vm.saveBudget("100")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.message != null)

        vm.dismissMessage()
        advanceUntilIdle()

        assertNull(vm.uiState.value.message)
    }
}
