package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintreker.data.local.ExpenseEntity
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.Balance
import com.example.fintreker.domain.calculateBalance
import com.example.fintreker.domain.monthRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val balance: Balance, val recent: List<ExpenseEntity>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * Головний екран балансу: залишок місячного бюджету = ліміт (EncryptedSharedPreferences)
 * мінус сума витрат за місяць (Room). Усе реактивно — нова витрата одразу змінює баланс.
 */
class HomeViewModel(
    expenseRepository: ExpenseRepository,
    secureStore: SecureStore,
    clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    /** Момент, на який побудовано екран (для заголовка місяця в UI). */
    val monthMillis: Long = clock()
    private val month = monthRange(monthMillis)

    val uiState: StateFlow<HomeUiState> = buildState(expenseRepository, secureStore)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HomeUiState.Loading
        )

    private fun buildState(
        expenseRepository: ExpenseRepository,
        secureStore: SecureStore
    ): Flow<HomeUiState> {
        // Явний тип Flow<HomeUiState>, щоб catch міг емітити і Success, і Error.
        val state: Flow<HomeUiState> = combine(
            expenseRepository.observeMonthTotal(month),
            expenseRepository.observeExpenses(category = null, limit = RECENT_COUNT),
            secureStore.budgetLimit
        ) { spent, recent, budget ->
            HomeUiState.Success(calculateBalance(budget, spent), recent)
        }
        return state.catch { error ->
            emit(HomeUiState.Error(error.message ?: "Не вдалося прочитати дані"))
        }
    }

    companion object {
        const val RECENT_COUNT = 5
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
