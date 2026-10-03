package com.example.fintreker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ExpenseEntity::class, RateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun rateDao(): RateDao

    companion object {
        private const val DB_NAME = "fintreker.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                // Приклад міграції (Лаб 3) див. у README: MIGRATION_1_2 для нового поля.
                .build()
    }
}
