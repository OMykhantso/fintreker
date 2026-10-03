package com.example.fintreker.data

import com.example.fintreker.domain.Expense
import com.example.fintreker.domain.Category
import kotlin.random.Random

/** Mock-дані витрат для демонстрації діаграми та пагінації (Лаб 1, AI-завдання). */
object MockData {

    private const val HOUR_MS = 3_600_000L

    private data class Template(val category: Category, val min: Int, val max: Int, val notes: List<String>)

    private val templates = listOf(
        Template(Category.FOOD, 80, 650, listOf("Сільпо", "Кафе", "Обід на роботі", "Піца", "АТБ")),
        Template(Category.TRANSPORT, 20, 400, listOf("Метро", "Таксі", "Пальне", "Проїзний")),
        Template(Category.HOUSING, 300, 3500, listOf("Комуналка", "Інтернет", "Оренда")),
        Template(Category.ENTERTAINMENT, 100, 900, listOf("Кіно", "Підписка", "Концерт", "Гра")),
        Template(Category.HEALTH, 60, 1200, listOf("Аптека", "Лікар", "Вітаміни")),
        Template(Category.OTHER, 30, 500, listOf("Подарунок", "Канцелярія", ""))
    )

    /**
     * Детермінований набір із [count] витрат за останні ~місяць (найновіші — біля [nowMillis]).
     * Одного seed завжди дає однаковий результат — зручно для тестів.
     */
    fun generate(nowMillis: Long, count: Int = 45, seed: Long = 42L): List<Expense> {
        val random = Random(seed)
        return List(count) { index ->
            val template = templates[random.nextInt(templates.size)]
            val hoursAgo = if (index < 30) index * 8L else 240L + (index - 30) * 30L
            Expense(
                amount = random.nextInt(template.min, template.max + 1).toDouble(),
                category = template.category.name,
                timestamp = nowMillis - hoursAgo * HOUR_MS,
                note = template.notes[random.nextInt(template.notes.size)]
            )
        }
    }
}
