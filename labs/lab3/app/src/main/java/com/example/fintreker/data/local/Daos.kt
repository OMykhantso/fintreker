package com.example.fintreker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fintreker.domain.CategoryTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun clear()

    /**
     * Реактивний список витрат (найновіші першими) з необов'язковим фільтром за категорією.
     * Збільшення [limit] = «підвантаження наступної сторінки» (Infinite Scroll, Лаб 6).
     */
    @Query(
        "SELECT * FROM expenses " +
            "WHERE (:category IS NULL OR category = :category) " +
            "ORDER BY timestamp DESC, id DESC " +
            "LIMIT :limit"
    )
    fun observeExpenses(category: String?, limit: Int): Flow<List<ExpenseEntity>>

    /** Сума витрат за період (півінтервал `[from, to)`). */
    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) FROM expenses " +
            "WHERE timestamp >= :fromMillis AND timestamp < :toMillis"
    )
    fun observeTotal(fromMillis: Long, toMillis: Long): Flow<Double>

    /** Агрегація за поточний місяць: `SUM(amount) GROUP BY category` (Лаб 3, AI-завдання). */
    @Query(
        "SELECT category, SUM(amount) AS total FROM expenses " +
            "WHERE timestamp >= :fromMillis AND timestamp < :toMillis " +
            "GROUP BY category ORDER BY total DESC"
    )
    fun observeCategoryTotals(fromMillis: Long, toMillis: Long): Flow<List<CategoryTotal>>
}
