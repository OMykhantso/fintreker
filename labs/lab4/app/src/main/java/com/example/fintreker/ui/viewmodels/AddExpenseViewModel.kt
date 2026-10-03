package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.AmountError
import com.example.fintreker.domain.AmountValidation
import com.example.fintreker.domain.BudgetStatus
import com.example.fintreker.domain.Category
import com.example.fintreker.domain.calculateBalance
import com.example.fintreker.domain.monthRange
import com.example.fintreker.domain.validateAmount
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddExpenseUiState(
    val amountText: String = "",
    val category: Category = Category.FOOD,
    val note: String = "",
    val amountError: AmountError? = null,
    val isSaving: Boolean = false,
    /** Одноразова подія «збережено» — UI перейде назад і викличе [AddExpenseViewModel.onSavedHandled]. */
    val saved: Boolean = false,
    /** Після збереження місячний ліміт перевищено. */
    val limitExceeded: Boolean = false,
    /** Помилка запису в БД (форма лишається заповненою, можна повторити). */
    val saveError: String? = null
)

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val secureStore: SecureStore,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun onAmountChange(raw: String) {
        // Дозволяємо лише цифри та роздільник; усе інше відсікаємо ще на вводі.
        val filtered = raw.filter { it in '0'..'9' || it == '.' || it == ',' }
        _uiState.update { it.copy(amountText = filtered, amountError = null) }
    }

    fun onCategoryChange(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note.take(MAX_NOTE_LENGTH)) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.saved) return

        when (val validation = validateAmount(state.amountText)) {
            is AmountValidation.Invalid -> {
                _uiState.update { it.copy(amountError = validation.error) }
            }
            is AmountValidation.Valid -> {
                // Синхронно блокуємо повторні натискання ще до запуску корутини.
                _uiState.update { it.copy(isSaving = true, amountError = null, saveError = null) }
                viewModelScope.launch {
                    try {
                        val now = clock()
                        expenseRepository.add(validation.amount, state.category, state.note.trim(), now)

                        val spent = expenseRepository.monthTotal(monthRange(now))
                        val status = calculateBalance(secureStore.budgetLimit.value, spent).status
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saved = true,
                                limitExceeded = status == BudgetStatus.EXCEEDED
                            )
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(isSaving = false, saveError = "Не вдалося зберегти витрату. Спробуйте ще раз")
                        }
                    }
                }
            }
        }
    }

    /** Скидає форму після того, як UI обробив подію «збережено». */
    fun onSavedHandled() {
        _uiState.value = AddExpenseUiState()
    }

    companion object {
        const val MAX_NOTE_LENGTH = 120
    }
}
