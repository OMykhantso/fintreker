package com.example.fintreker.domain

/** Витрата. У Лаб 3 вона стане Room-сутністю `ExpenseEntity` у шарі `data`. */
data class Expense(
    val id: Long = 0,
    val amount: Double,
    /** Ім'я [Category] (enum name). */
    val category: String,
    val timestamp: Long,
    val note: String = ""
)
