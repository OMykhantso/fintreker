package com.example.fintreker.data.security

import kotlinx.coroutines.flow.StateFlow

/**
 * Сховище чутливих налаштувань (PIN, ліміт бюджету). Інтерфейс відокремлено від Android-реалізації
 * [SecureStorage], щоб ViewModel можна було тестувати з MockK без Context.
 */
interface SecureStore {
    /** Місячний ліміт бюджету в гривнях; `0.0` — ліміт не задано. */
    val budgetLimit: StateFlow<Double>
    val pinEnabled: StateFlow<Boolean>

    fun setBudgetLimit(amount: Double)
    fun setPin(pin: String)
    fun clearPin()
    fun verifyPin(pin: String): Boolean
}
