package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintreker.data.DemoData
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.AmountError
import com.example.fintreker.domain.AmountValidation
import com.example.fintreker.domain.ReminderController
import com.example.fintreker.domain.isValidPin
import com.example.fintreker.domain.validateBudgetLimit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val budgetLimit: Double = 0.0,
    val pinEnabled: Boolean = false,
    val reminderEnabled: Boolean = true,
    val budgetError: AmountError? = null,
    val pinError: String? = null,
    /** Одноразове повідомлення для Snackbar; UI викликає [SettingsViewModel.dismissMessage]. */
    val message: String? = null
)

class SettingsViewModel(
    private val secureStore: SecureStore,
    private val expenseRepository: ExpenseRepository,
    private val reminderController: ReminderController,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private data class Feedback(
        val budgetError: AmountError? = null,
        val pinError: String? = null,
        val message: String? = null
    )

    private val feedback = MutableStateFlow(Feedback())

    val uiState: StateFlow<SettingsUiState> = combine(
        secureStore.budgetLimit,
        secureStore.pinEnabled,
        secureStore.reminderEnabled,
        feedback
    ) { budget, pinEnabled, reminderEnabled, feedback ->
        SettingsUiState(
            budgetLimit = budget,
            pinEnabled = pinEnabled,
            reminderEnabled = reminderEnabled,
            budgetError = feedback.budgetError,
            pinError = feedback.pinError,
            message = feedback.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = SettingsUiState()
    )

    fun saveBudget(input: String) {
        when (val validation = validateBudgetLimit(input)) {
            is AmountValidation.Invalid -> feedback.update {
                it.copy(budgetError = validation.error, message = null)
            }
            is AmountValidation.Valid -> {
                secureStore.setBudgetLimit(validation.amount)
                feedback.update { it.copy(budgetError = null, message = "Ліміт бюджету збережено") }
            }
        }
    }

    fun clearBudget() {
        secureStore.setBudgetLimit(0.0)
        feedback.update { it.copy(budgetError = null, message = "Ліміт бюджету вимкнено") }
    }

    fun setPin(pin: String) {
        if (!isValidPin(pin)) {
            feedback.update { it.copy(pinError = "PIN має складатись із 4 цифр", message = null) }
            return
        }
        secureStore.setPin(pin)
        feedback.update { it.copy(pinError = null, message = "PIN-код встановлено") }
    }

    fun disablePin() {
        secureStore.clearPin()
        feedback.update { it.copy(pinError = null, message = "PIN-код вимкнено") }
    }

    fun setReminderEnabled(enabled: Boolean) {
        secureStore.setReminderEnabled(enabled)
        if (enabled) reminderController.scheduleDaily() else reminderController.cancelDaily()
    }

    fun sendTestReminder() {
        reminderController.sendTestNow()
        feedback.update { it.copy(message = "Тестове нагадування надіслано") }
    }

    fun addDemoData() {
        viewModelScope.launch {
            expenseRepository.addAll(DemoData.generate(clock()))
            feedback.update { it.copy(message = "Додано демо-витрати") }
        }
    }

    fun clearExpenses() {
        viewModelScope.launch {
            expenseRepository.clear()
            feedback.update { it.copy(message = "Усі витрати видалено") }
        }
    }

    fun dismissMessage() {
        feedback.update { it.copy(message = null) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
