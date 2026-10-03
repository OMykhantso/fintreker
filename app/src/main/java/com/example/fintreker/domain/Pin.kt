package com.example.fintreker.domain

import java.security.MessageDigest
import java.security.SecureRandom

const val PIN_LENGTH = 4

fun isValidPin(pin: String): Boolean = pin.length == PIN_LENGTH && pin.all { it in '0'..'9' }

/** Випадкова «сіль» (16 байт у hex) для хешування PIN-коду. */
fun generateSalt(random: SecureRandom = SecureRandom()): String {
    val bytes = ByteArray(16)
    random.nextBytes(bytes)
    return bytes.toHex()
}

/** PIN ніколи не зберігається у відкритому вигляді — лише SHA-256(salt + pin). */
fun hashPin(pin: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest((salt + pin).toByteArray(Charsets.UTF_8))
    return digest.toHex()
}

/** Порівняння за сталий час, щоб не давати інформації про збіг по префіксу. */
fun pinMatches(pin: String, salt: String, expectedHash: String): Boolean =
    MessageDigest.isEqual(
        hashPin(pin, salt).toByteArray(Charsets.UTF_8),
        expectedHash.toByteArray(Charsets.UTF_8)
    )

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
