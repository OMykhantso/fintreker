package com.example.fintreker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Транзакція-витрата: id, сума, категорія, час, нотатка (Лаб 3). */
@Entity(
    tableName = "expenses",
    indices = [Index("timestamp"), Index("category")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    /** Ім'я [com.example.fintreker.domain.Category] (enum name). */
    val category: String,
    val timestamp: Long,
    val note: String = ""
)
