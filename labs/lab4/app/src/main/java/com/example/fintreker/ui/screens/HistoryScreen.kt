package com.example.fintreker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fintreker.domain.Category
import com.example.fintreker.ui.components.ExpenseRow
import com.example.fintreker.ui.components.ShimmerBox
import com.example.fintreker.ui.theme.color
import com.example.fintreker.ui.viewmodels.HistoryUiState
import com.example.fintreker.ui.viewmodels.HistoryViewModel

/**
 * Історія витрат з фільтром за категорією ([initialCategory] приходить з типізованого маршруту
 * `ExpenseHistoryRoute(category)`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    initialCategory: String?
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialCategory) { viewModel.setCategory(initialCategory) }

    Scaffold(topBar = { TopAppBar(title = { Text("Історія витрат") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CategoryFilterRow(
                selected = (state as? HistoryUiState.Success)?.category,
                onSelect = { viewModel.setCategory(it?.name) }
            )

            when (val current = state) {
                HistoryUiState.Loading -> HistoryLoading()
                is HistoryUiState.Success -> HistoryList(
                    state = current,
                    onDelete = viewModel::delete
                )
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

@Composable
private fun HistoryLoading() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(6) { ShimmerBox(Modifier.fillMaxWidth().height(56.dp)) }
    }
}

@Composable
private fun HistoryList(
    state: HistoryUiState.Success,
    onDelete: (Long) -> Unit
) {
    if (state.items.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                "Витрат немає. Додайте їх на головному екрані або створіть демо-дані в налаштуваннях.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.items, key = { it.id }) { expense ->
                ExpenseRow(expense, onDelete = { onDelete(expense.id) })
                HorizontalDivider()
            }
        }
    }
}
