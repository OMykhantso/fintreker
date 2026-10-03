package com.example.fintreker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fintreker.data.ExpenseStore
import com.example.fintreker.domain.Category
import com.example.fintreker.ui.components.ExpenseRow
import com.example.fintreker.ui.theme.color

/**
 * Історія витрат з фільтром за категорією. [initialCategory] приходить з типізованого маршруту
 * `ExpenseHistoryRoute(category)` — це і є передача параметра між екранами (Лаб 2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(initialCategory: String?) {
    var selected by remember(initialCategory) { mutableStateOf(Category.fromName(initialCategory)) }
    val filter = selected
    val items = ExpenseStore.expenses.filter { filter == null || it.category == filter.name }

    Scaffold(topBar = { TopAppBar(title = { Text("Історія витрат") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CategoryFilterRow(selected = filter, onSelect = { selected = it })

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Витрат немає.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { expense ->
                        ExpenseRow(expense, onDelete = { ExpenseStore.delete(expense.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(selected: Category?, onSelect: (Category?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Усі") }
            )
        }
        items(Category.entries) { category ->
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
