package com.example.fintreker.viewmodels

import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.ui.viewmodels.PinViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinViewModelTest {

    private fun store(pinSet: Boolean, correctPin: String = "1234") = mockk<SecureStore> {
        every { pinEnabled } returns MutableStateFlow(pinSet)
        every { verifyPin(any()) } answers { firstArg<String>() == correctPin }
    }

    private fun PinViewModel.enter(pin: String) = pin.forEach { onDigit(it) }

    @Test
    fun `without a pin the app is not locked`() {
        val vm = PinViewModel(store(pinSet = false))

        assertFalse(vm.uiState.value.isLocked)
    }

    @Test
    fun `with a pin the app starts locked`() {
        val vm = PinViewModel(store(pinSet = true))

        assertTrue(vm.uiState.value.isLocked)
        assertEquals(0, vm.uiState.value.enteredLength)
    }

    @Test
    fun `correct pin unlocks`() {
        val vm = PinViewModel(store(pinSet = true))

        vm.enter("1234")

        assertFalse(vm.uiState.value.isLocked)
        assertFalse(vm.uiState.value.showError)
    }

    @Test
    fun `wrong pin keeps the lock, shows error and counts the attempt`() {
        val vm = PinViewModel(store(pinSet = true))

        vm.enter("0000")

        val state = vm.uiState.value
        assertTrue(state.isLocked)
        assertTrue(state.showError)
        assertEquals(1, state.failedAttempts)
        assertEquals(0, state.enteredLength)
    }

    @Test
    fun `can retry after a wrong pin`() {
        val vm = PinViewModel(store(pinSet = true))
        vm.enter("0000")

        vm.enter("1234")

        assertFalse(vm.uiState.value.isLocked)
        assertEquals(1, vm.uiState.value.failedAttempts)
    }

    @Test
    fun `progress dots follow the typed digits and backspace`() {
        val vm = PinViewModel(store(pinSet = true))

        vm.onDigit('1')
        vm.onDigit('2')
        assertEquals(2, vm.uiState.value.enteredLength)

        vm.onBackspace()
        assertEquals(1, vm.uiState.value.enteredLength)
    }

    @Test
    fun `typing after an error hides the error`() {
        val vm = PinViewModel(store(pinSet = true))
        vm.enter("0000")
        assertTrue(vm.uiState.value.showError)

        vm.onDigit('1')

        assertFalse(vm.uiState.value.showError)
    }

    @Test
    fun `backspace on an empty entry is harmless`() {
        val vm = PinViewModel(store(pinSet = true))

        vm.onBackspace()

        assertEquals(0, vm.uiState.value.enteredLength)
    }

    @Test
    fun `non digit input is ignored`() {
        val secureStore = store(pinSet = true)
        val vm = PinViewModel(secureStore)

        vm.enter("12ab")

        assertEquals(2, vm.uiState.value.enteredLength)
        verify(exactly = 0) { secureStore.verifyPin(any()) }
    }

    @Test
    fun `input is ignored when there is no lock`() {
        val secureStore = store(pinSet = false)
        val vm = PinViewModel(secureStore)

        vm.enter("1234")

        verify(exactly = 0) { secureStore.verifyPin(any()) }
    }
}
