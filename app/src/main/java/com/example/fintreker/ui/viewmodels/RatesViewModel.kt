package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fintreker.data.repository.RatesRepository
import com.example.fintreker.data.repository.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Помилки мережі обробляються через sealed UiState (Rules.md, п. 10). */
sealed interface RatesUiState {
    data object Loading : RatesUiState

    /**
     * Є курс у кеші Room. [isOffline] = true, якщо останнє оновлення з мережі не вдалось,
     * тобто показуємо застарілі, але робочі дані.
     */
    data class Success(
        val usdRate: Double?,
        val eurRate: Double?,
        val exchangeDate: String?,
        val isOffline: Boolean,
        val isRefreshing: Boolean
    ) : RatesUiState

    /** Кешу немає і мережа недоступна — показати помилку та кнопку «Повторити». */
    data class Error(val message: String) : RatesUiState
}

class RatesViewModel(
    private val ratesRepository: RatesRepository
) : ViewModel() {

    private sealed interface Refresh {
        data object Idle : Refresh
        data object InProgress : Refresh
        data class Failed(val message: String) : Refresh
    }

    private val refresh = MutableStateFlow<Refresh>(Refresh.Idle)

    val uiState: StateFlow<RatesUiState> = combine(ratesRepository.rates, refresh) { rates, refresh ->
        when {
            rates.isNotEmpty() -> RatesUiState.Success(
                usdRate = rates.firstOrNull { it.code == RatesRepository.USD }?.rate,
                eurRate = rates.firstOrNull { it.code == RatesRepository.EUR }?.rate,
                exchangeDate = rates.firstOrNull { it.code == RatesRepository.USD }?.exchangeDate
                    ?: rates.first().exchangeDate,
                isOffline = refresh is Refresh.Failed,
                isRefreshing = refresh is Refresh.InProgress
            )
            refresh is Refresh.Failed -> RatesUiState.Error(refresh.message)
            else -> RatesUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = RatesUiState.Loading
    )

    init {
        refresh()
    }

    fun refresh() {
        if (refresh.value is Refresh.InProgress) return
        viewModelScope.launch {
            refresh.update { Refresh.InProgress }
            val result = ratesRepository.refresh()
            refresh.update {
                when (result) {
                    SyncResult.Success -> Refresh.Idle
                    is SyncResult.Failure -> Refresh.Failed(result.message)
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
