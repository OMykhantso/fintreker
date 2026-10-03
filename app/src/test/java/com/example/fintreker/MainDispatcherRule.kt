package com.example.fintreker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Підміняє `Dispatchers.Main` на [StandardTestDispatcher], щоб `viewModelScope` працював у JVM-тестах
 * і корутини виконувались лише під контролем `advanceUntilIdle()` (детермінований порядок).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * `StateFlow`, створений через `stateIn(WhileSubscribed)`, оживає лише за наявності підписника —
 * у тесті підписуємось у `backgroundScope` (скасовується автоматично в кінці `runTest`).
 */
fun TestScope.collectInBackground(flow: Flow<*>) {
    backgroundScope.launch { flow.collect { } }
}
