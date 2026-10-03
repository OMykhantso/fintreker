package com.example.fintreker.navigation

import kotlinx.serialization.Serializable

/**
 * Типізовані маршрути (Type-Safe Navigation Compose 2.8+, Лаб 2): жодних «магічних рядків»,
 * параметри екранів — це поля `@Serializable` класів.
 */

/** Головний екран балансу. */
@Serializable
object HomeRoute

/** Екран додавання нової витрати. */
@Serializable
object AddExpenseRoute

/** Історія витрат; [category] — необов'язковий фільтр (ім'я `Category`, `null` = усі). */
@Serializable
data class ExpenseHistoryRoute(val category: String? = null)

/** Детальна аналітика: розподіл витрат за категоріями. */
@Serializable
object AnalyticsRoute
