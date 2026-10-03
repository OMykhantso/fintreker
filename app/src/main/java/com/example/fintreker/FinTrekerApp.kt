package com.example.fintreker

import android.app.Application
import com.example.fintreker.work.NotificationHelper

class FinTrekerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        NotificationHelper.createChannel(this)
        // KEEP: якщо розклад уже існує, запуск застосунку його не скидає.
        if (container.secureStore.reminderEnabled.value) {
            container.reminderController.scheduleDaily()
        }
    }
}
