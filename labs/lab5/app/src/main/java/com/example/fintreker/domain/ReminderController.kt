package com.example.fintreker.domain

/**
 * Абстракція над плануванням щоденного нагадування (реалізація на WorkManager — у пакеті `work`).
 * Інтерфейс дозволяє тестувати ViewModel без Android-залежностей.
 */
interface ReminderController {
    /** Запланувати щоденне нагадування о 20:00; якщо вже заплановано — залишити як є. */
    fun scheduleDaily()

    /** Перерахувати розклад (наприклад, після перезавантаження телефону). */
    fun rescheduleDaily()

    fun cancelDaily()

    /** Одноразово показати сповіщення негайно (кнопка «Тестове нагадування»). */
    fun sendTestNow()
}
