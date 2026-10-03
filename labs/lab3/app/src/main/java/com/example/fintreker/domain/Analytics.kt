package com.example.fintreker.domain

/** Результат агрегації `SUM(amount) GROUP BY category` з Room (колонки `category`, `total`). */
data class CategoryTotal(
    val category: String,
    val total: Double
)

/** Частка категорії у витратах — для кругової діаграми та легенди. */
data class CategoryShare(
    val category: Category,
    val total: Double,
    val fraction: Float
)

/**
 * Перетворює агреговані суми у відсоткові частки. Ігнорує нульові/від'ємні/некоректні суми,
 * зливає невідомі категорії в [Category.OTHER], сортує за спаданням.
 */
fun calculateShares(totals: List<CategoryTotal>): List<CategoryShare> {
    val merged = totals
        .filter { it.total.isFinite() && it.total > 0.0 }
        .groupBy { Category.fromNameOrOther(it.category) }
        .mapValues { (_, items) -> items.sumOf { it.total } }

    val sum = merged.values.sum()
    if (sum <= 0.0) return emptyList()

    return merged.entries
        .map { (category, total) -> CategoryShare(category, total, (total / sum).toFloat()) }
        .sortedByDescending { it.total }
}
