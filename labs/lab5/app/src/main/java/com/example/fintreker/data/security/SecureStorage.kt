package com.example.fintreker.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.fintreker.domain.generateSalt
import com.example.fintreker.domain.hashPin
import com.example.fintreker.domain.isValidPin
import com.example.fintreker.domain.pinMatches
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Реалізація [SecureStore] на EncryptedSharedPreferences (AES256-GCM + Android Keystore) — Лаб 3.
 * PIN зберігається лише як SHA-256 хеш із сіллю; ліміт бюджету — у зашифрованому вигляді.
 */
class SecureStorage(context: Context) : SecureStore {

    private val prefs: SharedPreferences = createPrefs(context.applicationContext)

    private val _budgetLimit = MutableStateFlow(readBudget())
    override val budgetLimit: StateFlow<Double> = _budgetLimit.asStateFlow()

    private val _pinEnabled = MutableStateFlow(prefs.contains(KEY_PIN_HASH))
    override val pinEnabled: StateFlow<Boolean> = _pinEnabled.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(prefs.getBoolean(KEY_REMINDER, true))
    override val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    override fun setBudgetLimit(amount: Double) {
        val value = if (amount.isFinite() && amount > 0.0) amount else 0.0
        prefs.edit().putString(KEY_BUDGET, value.toString()).apply()
        _budgetLimit.value = value
    }

    override fun setPin(pin: String) {
        require(isValidPin(pin)) { "PIN має складатись із 4 цифр" }
        val salt = generateSalt()
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hashPin(pin, salt))
            .apply()
        _pinEnabled.value = true
    }

    override fun clearPin() {
        prefs.edit().remove(KEY_PIN_SALT).remove(KEY_PIN_HASH).apply()
        _pinEnabled.value = false
    }

    override fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val hash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return pinMatches(pin, salt, hash)
    }

    override fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER, enabled).apply()
        _reminderEnabled.value = enabled
    }

    private fun readBudget(): Double =
        prefs.getString(KEY_BUDGET, null)?.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: 0.0

    private companion object {
        const val FILE_NAME = "fintreker_secure_prefs"
        const val KEY_BUDGET = "budget_limit"
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
        const val KEY_REMINDER = "reminder_enabled"

        fun createPrefs(context: Context): SharedPreferences = try {
            create(context)
        } catch (e: Exception) {
            // Ключ у Keystore втрачено (відновлення з бекапу, скидання захисту екрана тощо) —
            // зашифровані дані вже не прочитати, тож починаємо з чистого файлу (див. FAQ, п. 8).
            context.deleteSharedPreferences(FILE_NAME)
            create(context)
        }

        private fun create(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
