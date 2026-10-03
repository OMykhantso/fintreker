package com.example.fintreker.viewmodels

import com.example.fintreker.MainDispatcherRule
import com.example.fintreker.collectInBackground
import com.example.fintreker.data.local.RateEntity
import com.example.fintreker.data.repository.RatesRepository
import com.example.fintreker.data.repository.SyncResult
import com.example.fintreker.ui.viewmodels.RatesUiState
import com.example.fintreker.ui.viewmodels.RatesViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Offline-First: помилки мережі перетворюються на стани sealed [RatesUiState]. */
@OptIn(ExperimentalCoroutinesApi::class)
class RatesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val cache = MutableStateFlow<List<RateEntity>>(emptyList())
    private val repository = mockk<RatesRepository> { every { rates } returns cache }

    private val cachedRates = listOf(
        RateEntity("USD", 41.5, "01.10.2026", 1L),
        RateEntity("EUR", 48.0, "01.10.2026", 1L)
    )

    @Test
    fun `starts in Loading`() = runTest {
        coEvery { repository.refresh() } returns SyncResult.Success

        val vm = RatesViewModel(repository)

        assertEquals(RatesUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `cached rates and successful refresh give a fresh Success`() = runTest {
        cache.value = cachedRates
        coEvery { repository.refresh() } returns SyncResult.Success
        val vm = RatesViewModel(repository)

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        assertEquals(
            RatesUiState.Success(
                usdRate = 41.5,
                eurRate = 48.0,
                exchangeDate = "01.10.2026",
                isOffline = false,
                isRefreshing = false
            ),
            vm.uiState.value
        )
    }

    @Test
    fun `no internet but cached rates still give Success flagged as offline`() = runTest {
        cache.value = cachedRates
        coEvery { repository.refresh() } returns SyncResult.Failure("Немає з'єднання з мережею")
        val vm = RatesViewModel(repository)

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        val state = vm.uiState.value as RatesUiState.Success
        assertEquals(true, state.isOffline)
        assertEquals(41.5, state.usdRate!!, 0.0)
        assertEquals(48.0, state.eurRate!!, 0.0)
    }

    @Test
    fun `no internet and empty cache gives Error with the reason`() = runTest {
        coEvery { repository.refresh() } returns SyncResult.Failure("Немає з'єднання з мережею")
        val vm = RatesViewModel(repository)

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        assertEquals(RatesUiState.Error("Немає з'єднання з мережею"), vm.uiState.value)
    }

    @Test
    fun `manual retry recovers from Error once the network is back`() = runTest {
        coEvery { repository.refresh() } returnsMany listOf(
            SyncResult.Failure("Немає з'єднання з мережею"),
            SyncResult.Success
        )
        val vm = RatesViewModel(repository)
        collectInBackground(vm.uiState)
        advanceUntilIdle()
        assertEquals(RatesUiState.Error("Немає з'єднання з мережею"), vm.uiState.value)

        cache.value = cachedRates // репозиторій записав курси в Room
        vm.refresh()
        advanceUntilIdle()

        val state = vm.uiState.value as RatesUiState.Success
        assertEquals(false, state.isOffline)
        assertEquals(41.5, state.usdRate!!, 0.0)
    }

    @Test
    fun `refresh in progress is reported and parallel refreshes are ignored`() = runTest {
        cache.value = cachedRates
        val gate = CompletableDeferred<SyncResult>()
        coEvery { repository.refresh() } coAnswers { gate.await() }
        val vm = RatesViewModel(repository)
        collectInBackground(vm.uiState)
        runCurrent()

        vm.refresh() // уже триває — має бути проігноровано
        runCurrent()

        assertEquals(true, (vm.uiState.value as RatesUiState.Success).isRefreshing)
        coVerify(exactly = 1) { repository.refresh() }

        gate.complete(SyncResult.Success)
        advanceUntilIdle()

        assertEquals(false, (vm.uiState.value as RatesUiState.Success).isRefreshing)
    }

    @Test
    fun `missing EUR in cache gives null eur rate`() = runTest {
        cache.value = listOf(RateEntity("USD", 41.5, "01.10.2026", 1L))
        coEvery { repository.refresh() } returns SyncResult.Success
        val vm = RatesViewModel(repository)

        collectInBackground(vm.uiState)
        advanceUntilIdle()

        val state = vm.uiState.value as RatesUiState.Success
        assertEquals(41.5, state.usdRate!!, 0.0)
        assertEquals(null, state.eurRate)
    }
}
