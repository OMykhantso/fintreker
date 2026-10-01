package com.example.fintreker.data.repository

import com.example.fintreker.data.local.ExpenseDao
import com.example.fintreker.data.local.ExpenseEntity
import com.example.fintreker.domain.Category
import com.example.fintreker.domain.CategoryTotal
import com.example.fintreker.domain.TimeRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Єдина точка доступу до витрат. UI підписується на Flow із Room і ніколи не читає «напряму». */
class ExpenseRepository(private val dao: ExpenseDao) {

    fun observeExpenses(category: String?, limit: Int): Flow<List<ExpenseEntity>> =
        dao.observeExpenses(category, limit)

    fun observeMonthTotal(range: TimeRange): Flow<Double> =
        dao.observeTotal(range.startMillis, range.endMillis)

    fun observeCategoryTotals(range: TimeRange): Flow<List<CategoryTotal>> =
        dao.observeCategoryTotals(range.startMillis, range.endMillis)

    suspend fun monthTotal(range: TimeRange): Double = observeMonthTotal(range).first()

    suspend fun add(amount: Double, category: Category, note: String, timestamp: Long) {
        dao.insert(
            ExpenseEntity(
                amount = amount,
                category = category.name,
                timestamp = timestamp,
                note = note
            )
        )
    }

    suspend fun addAll(expenses: List<ExpenseEntity>) = dao.insertAll(expenses)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clear() = dao.clear()
}
