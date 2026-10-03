package com.example.fintreker.domain

// ---------------------------------------------------------------------------
// Валідація суми та ліміту бюджету
// ---------------------------------------------------------------------------

const val MAX_AMOUNT = 1_000_000_000.0

enum class AmountError(val message: String) {
    EMPTY("Введіть суму"),
    NOT_A_NUMBER("Некоректне число"),
    NOT_POSITIVE("Сума має бути більшою за 0"),
    TOO_LARGE("Сума занадто велика")
}

sealed interface AmountValidation {
    data class Valid(val amount: Double) : AmountValidation
    data class Invalid(val error: AmountError) : AmountValidation
}

// "5", "5.", "5.25", ".5" — але не "1e3", "5d", "NaN", "Infinity", "0x10"
private val NUMBER_REGEX = Regex("""^(\d+\.?\d*|\.\d+)$""")

/** Округлення до копійок. */
fun roundMoney(value: Double): Double = Math.round(value * 100.0) / 100.0

/**
 * Перевіряє введену користувачем суму: приймає кому та крапку як роздільник,
 * відкидає порожні значення, не-числа, від'ємні, нульові та надто великі суми.
 */
fun validateAmount(input: String): AmountValidation {
    val normalized = input.trim().replace(" ", "").replace(',', '.')
    if (normalized.isEmpty()) return AmountValidation.Invalid(AmountError.EMPTY)

    val negative = normalized.startsWith("-")
    val body = if (negative) normalized.substring(1) else normalized
    if (!NUMBER_REGEX.matches(body)) return AmountValidation.Invalid(AmountError.NOT_A_NUMBER)
    if (negative) return AmountValidation.Invalid(AmountError.NOT_POSITIVE)

    val value = body.toDoubleOrNull()
    if (value == null || !value.isFinite()) return AmountValidation.Invalid(AmountError.NOT_A_NUMBER)
    if (value > MAX_AMOUNT) return AmountValidation.Invalid(AmountError.TOO_LARGE)

    val rounded = roundMoney(value)
    return if (rounded <= 0.0) {
        AmountValidation.Invalid(AmountError.NOT_POSITIVE)
    } else {
        AmountValidation.Valid(rounded)
    }
}

/** Ліміт бюджету підпорядковується тим самим правилам, що й сума витрати (> 0). */
fun validateBudgetLimit(input: String): AmountValidation = validateAmount(input)

// ---------------------------------------------------------------------------
// Валюти
// ---------------------------------------------------------------------------

enum class CurrencyCode(val symbol: String) {
    UAH("₴"),
    USD("$"),
    EUR("€")
}
