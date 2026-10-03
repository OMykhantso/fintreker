package com.example.fintreker.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.fintreker.FinTrekerApp

/**
 * Відновлення розкладу після перезавантаження телефону (`RECEIVE_BOOT_COMPLETED`, Лаб 5).
 * Заново вираховує затримку до найближчих 20:00, якщо нагадування увімкнене.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val container = (context.applicationContext as FinTrekerApp).container
        if (container.secureStore.reminderEnabled.value) {
            container.reminderController.rescheduleDaily()
        }
    }
}
