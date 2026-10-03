package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintreker.data.local.ExpenseEntity
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.domain.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(
        val items: List<ExpenseEntity>,
        /** Вибраний фільтр; `null` = усі категорії. */
        val category: Category?
    ) : HistoryUiState
}

/** Історія витрат з фільтром за категорією (параметр типізованого маршруту `ExpenseHistoryRoute`). */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = selectedCategory
        .flatMapLatest { name ->
            expenseRepository.observeExpenses(name, Int.MAX_VALUE)
                .map<List<ExpenseEntity>, HistoryUiState> { items ->
                    HistoryUiState.Success(items = items, category = Category.fromName(name))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HistoryUiState.Loading
        )

    fun setCategory(categoryName: String?) {
        selectedCategory.value = categoryName
    }

    fun delete(id: Long) {
        viewModelScope.launch { expenseRepository.delete(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
