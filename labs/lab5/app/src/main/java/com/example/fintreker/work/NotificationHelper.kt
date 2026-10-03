package com.example.fintreker.work

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fintreker.MainActivity
import com.example.fintreker.R
import com.example.fintreker.navigation.ADD_EXPENSE_DEEP_LINK

/** Канал, PendingIntent і показ сповіщення-нагадування (Лаб 5). */
object NotificationHelper {

    const val CHANNEL_ID = "daily_expense_reminder"
    private const val NOTIFICATION_ID = 2001
    private const val REQUEST_CODE_ADD_EXPENSE = 100

    /** З Android 8.0 (minSdk 26) сповіщення без каналу не показуються. Виклик ідемпотентний. */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Щоденне нагадування",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Нагадує зафіксувати витрати за день"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * PendingIntent, що відкриває екран додавання витрати через Deep Link `fintreker://add`.
     * CLEAR_TASK + NEW_TASK запускають чисту активність, тож навігація обробить URI з нуля.
     * FLAG_IMMUTABLE обов'язковий для Android 12+.
     */
    fun addExpensePendingIntent(context: Context): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ADD_EXPENSE_DEEP_LINK), context, MainActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_ADD_EXPENSE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** @return `true`, якщо сповіщення показано; `false`, якщо користувач не дав дозвіл. */
    @SuppressLint("MissingPermission") // дозвіл перевіряється вручну нижче
    fun showReminder(context: Context): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val manager = NotificationManagerCompat.from(context)
        if (!permissionGranted || !manager.areNotificationsEnabled()) return false

        createChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💰 FinTreker")
            .setContentText("Не забудьте зафіксувати сьогоднішні витрати!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(addExpensePendingIntent(context))
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
        return true
    }
}
