package com.example.fintreker.domain

/**
 * Єдине джерело правди для фінансових категорій: назва, емодзі та колір (Rules.md, п. 11).
 * Колір зберігається як ARGB-число, щоб domain не залежав від Compose.
 */
enum class Category(val label: String, val emoji: String, val colorArgb: Long) {
    FOOD("Їжа", "🍔", 0xFFEF6C00),
    TRANSPORT("Транспорт", "🚌", 0xFF1E88E5),
    HOUSING("Житло", "🏠", 0xFF6D4C41),
    ENTERTAINMENT("Розваги", "🎮", 0xFF8E24AA),
    HEALTH("Здоров'я", "💊", 0xFF43A047),
    OTHER("Інше", "📦", 0xFF757575);

    companion object {
        /** Безпечний пошук за іменем enum; `null`, якщо імені немає. */
        fun fromName(name: String?): Category? = entries.firstOrNull { it.name == name }

        /** Невідомі значення з БД трактуються як [OTHER]. */
        fun fromNameOrOther(name: String?): Category = fromName(name) ?: OTHER
    }
}
