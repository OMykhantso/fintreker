package com.example.fintreker.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Фонова задача: показує щоденне нагадування. Запускається WorkManager-ом навіть у Doze-режимі. */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showReminder(applicationContext)
        return Result.success()
    }
}
