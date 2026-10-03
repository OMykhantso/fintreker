package com.example.fintreker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fintreker.domain.Expense
import com.example.fintreker.domain.Category
import com.example.fintreker.domain.formatAmount
import com.example.fintreker.domain.formatDateTime
import com.example.fintreker.ui.theme.color

/** Вибір категорії витрати: FilterChip у рядках, що переносяться (Лаб 1). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    selected: Category,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Category.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text("${category.emoji} ${category.label}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.color().copy(alpha = 0.25f)
                )
            )
        }
    }
}

/** Рядок витрати в списках «Останні» та «Історія». */
@Composable
fun ExpenseRow(
    expense: Expense,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val category = Category.fromNameOrOther(expense.category)
    val details = listOfNotNull(
        formatDateTime(expense.timestamp),
        expense.note.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    ListItem(
        modifier = modifier,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(category.color().copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.emoji)
            }
        },
        headlineContent = { Text(category.label) },
        supportingContent = { Text(details) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "−${formatAmount(expense.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Видалити витрату")
                    }
                }
            }
        }
    )
}
