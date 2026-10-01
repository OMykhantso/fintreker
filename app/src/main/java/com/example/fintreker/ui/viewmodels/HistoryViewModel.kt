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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(
        val items: List<ExpenseEntity>,
        /** Вибраний фільтр; `null` = усі категорії. */
        val category: Category?,
        /** Чи є ще записи для підвантаження (Infinite Scroll). */
        val hasMore: Boolean
    ) : HistoryUiState
}

/**
 * Історія витрат з фільтром за категорією та пагінацією.
 * Пагінація реалізована через зростаючий `LIMIT` у реактивному запиті Room: [loadMore] збільшує
 * ліміт на [pageSize], Flow знову емітить, а UI лишається підписаним на єдине джерело правди.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val pageSize: Int = DEFAULT_PAGE_SIZE
) : ViewModel() {

    private data class Query(val category: String?, val limit: Int)

    private val query = MutableStateFlow(Query(category = null, limit = pageSize))

    val uiState: StateFlow<HistoryUiState> = query
        .flatMapLatest { q ->
            expenseRepository.observeExpenses(q.category, q.limit)
                .map<List<ExpenseEntity>, HistoryUiState> { items ->
                    HistoryUiState.Success(
                        items = items,
                        category = Category.fromName(q.category),
                        hasMore = items.size >= q.limit
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HistoryUiState.Loading
        )

    /** Змінює фільтр і скидає пагінацію до першої сторінки. */
    fun setCategory(categoryName: String?) {
        query.update { current ->
            if (current.category == categoryName) current else Query(categoryName, pageSize)
        }
    }

    /**
     * Підвантажує наступну сторінку. Захист від повторних викликів: ліміт збільшується
     * лише коли попередня сторінка вже повністю прийшла з БД (items.size >= limit).
     */
    fun loadMore() {
        val state = uiState.value as? HistoryUiState.Success ?: return
        query.update { current ->
            if (state.items.size >= current.limit) current.copy(limit = current.limit + pageSize) else current
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { expenseRepository.delete(id) }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
