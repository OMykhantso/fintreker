package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.domain.CategoryShare
import com.example.fintreker.domain.TimeRange
import com.example.fintreker.domain.calculateShares
import com.example.fintreker.domain.monthRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data object Empty : AnalyticsUiState
    data class Success(val total: Double, val shares: List<CategoryShare>) : AnalyticsUiState
    data class Error(val message: String) : AnalyticsUiState
}

/** Розподіл витрат поточного місяця за категоріями для кругової діаграми (Лаб 4). */
class AnalyticsViewModel(
    expenseRepository: ExpenseRepository,
    clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    val monthMillis: Long = clock()

    val uiState: StateFlow<AnalyticsUiState> = buildState(expenseRepository, monthRange(monthMillis))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = AnalyticsUiState.Loading
        )

    private fun buildState(
        expenseRepository: ExpenseRepository,
        range: TimeRange
    ): Flow<AnalyticsUiState> =
        expenseRepository.observeCategoryTotals(range)
            .map { totals ->
                val shares = calculateShares(totals)
                if (shares.isEmpty()) {
                    AnalyticsUiState.Empty
                } else {
                    AnalyticsUiState.Success(total = shares.sumOf { it.total }, shares = shares)
                }
            }
            .catch { error ->
                emit(AnalyticsUiState.Error(error.message ?: "Не вдалося прочитати дані"))
            }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
