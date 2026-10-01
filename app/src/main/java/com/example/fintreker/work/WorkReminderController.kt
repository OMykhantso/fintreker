package com.example.fintreker.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fintreker.domain.REMINDER_HOUR
import com.example.fintreker.domain.REMINDER_MINUTE
import com.example.fintreker.domain.ReminderController
import com.example.fintreker.domain.millisUntilNext
import java.util.concurrent.TimeUnit

/**
 * Планування щоденного нагадування через WorkManager (Лаб 5).
 *
 * `PeriodicWorkRequest` має період 24 год, а `initialDelay` вирівнює перший запуск на 20:00.
 * Після перезавантаження телефону [BootReceiver] викликає [rescheduleDaily], щоб заново
 * вирівняти розклад на 20:00.
 */
class WorkReminderController(context: Context) : ReminderController {

    private val appContext = context.applicationContext
    private val workManager: WorkManager get() = WorkManager.getInstance(appContext)

    override fun scheduleDaily() = enqueue(ExistingPeriodicWorkPolicy.KEEP)

    override fun rescheduleDaily() = enqueue(ExistingPeriodicWorkPolicy.UPDATE)

    override fun cancelDaily() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /** Лаб 5: OneTimeWorkRequest по кліку на тестову кнопку. */
    override fun sendTestNow() {
        workManager.enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
    }

    private fun enqueue(policy: ExistingPeriodicWorkPolicy) {
        val delayMillis = millisUntilNext(REMINDER_HOUR, REMINDER_MINUTE, System.currentTimeMillis())
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, policy, request)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "daily_expense_reminder"
    }
}
