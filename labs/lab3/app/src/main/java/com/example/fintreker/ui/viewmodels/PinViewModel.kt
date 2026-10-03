package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.PIN_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PinUiState(
    /** Скільки цифр уже введено (для індикатора-крапок). */
    val enteredLength: Int = 0,
    /** Чи треба показувати екран блокування. Без встановленого PIN — одразу `false`. */
    val isLocked: Boolean = false,
    val showError: Boolean = false,
    val failedAttempts: Int = 0
)

/** Екран блокування: PIN перевіряється проти SHA-256 хеша в EncryptedSharedPreferences. */
class PinViewModel(
    private val secureStore: SecureStore
) : ViewModel() {

    private var entered = ""

    private val _uiState = MutableStateFlow(PinUiState(isLocked = secureStore.pinEnabled.value))
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    fun onDigit(digit: Char) {
        if (!_uiState.value.isLocked || digit !in '0'..'9' || entered.length >= PIN_LENGTH) return
        entered += digit

        if (entered.length < PIN_LENGTH) {
            _uiState.update { it.copy(enteredLength = entered.length, showError = false) }
            return
        }

        val ok = secureStore.verifyPin(entered)
        entered = ""
        _uiState.update {
            if (ok) {
                it.copy(enteredLength = 0, isLocked = false, showError = false)
            } else {
                it.copy(enteredLength = 0, showError = true, failedAttempts = it.failedAttempts + 1)
            }
        }
    }

    fun onBackspace() {
        if (entered.isEmpty()) return
        entered = entered.dropLast(1)
        _uiState.update { it.copy(enteredLength = entered.length, showError = false) }
    }
}
