package com.example.fintreker.data

import androidx.compose.runtime.mutableStateListOf
import com.example.fintreker.domain.Expense

/**
 * Сховище витрат **в пам'яті** (Лаб 2): живе, поки працює процес.
 * У Лаб 3 його замінить база даних Room.
 */
object ExpenseStore {
    private val items = mutableStateListOf<Expense>()
    private var nextId = 1L

    init {
        MockData.generate(System.currentTimeMillis()).forEach { items.add(it.copy(id = nextId++)) }
    }

    /** Найновіші першими. Читання в Composable автоматично викликає рекомпозицію. */
    val expenses: List<Expense> get() = items

    fun add(amount: Double, category: String, note: String, timestamp: Long = System.currentTimeMillis()) {
        items.add(0, Expense(nextId++, amount, category, timestamp, note))
    }

    fun delete(id: Long) {
        items.removeAll { it.id == id }
    }
}
