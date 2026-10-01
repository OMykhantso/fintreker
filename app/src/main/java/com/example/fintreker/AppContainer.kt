package com.example.fintreker

import android.content.Context
import com.example.fintreker.data.local.AppDatabase
import com.example.fintreker.data.remote.RetrofitClient
import com.example.fintreker.data.repository.ExpenseRepository
import com.example.fintreker.data.repository.RatesRepository
import com.example.fintreker.data.security.SecureStorage
import com.example.fintreker.data.security.SecureStore
import com.example.fintreker.domain.ReminderController
import com.example.fintreker.work.WorkReminderController

/** Ручний DI-контейнер: єдине місце, де збираються залежності застосунку. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    val secureStore: SecureStore by lazy { SecureStorage(appContext) }

    val expenseRepository: ExpenseRepository by lazy { ExpenseRepository(database.expenseDao()) }

    val ratesRepository: RatesRepository by lazy {
        RatesRepository(RetrofitClient.nbuApi, database.rateDao())
    }

    val reminderController: ReminderController by lazy { WorkReminderController(appContext) }
}
