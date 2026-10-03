package com.example.fintreker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fintreker.domain.CategoryShare
import com.example.fintreker.domain.formatAmount
import com.example.fintreker.domain.formatMonthTitle
import com.example.fintreker.domain.formatPercent
import com.example.fintreker.ui.components.PieChart
import com.example.fintreker.ui.components.ShimmerBox
import com.example.fintreker.ui.theme.color
import com.example.fintreker.ui.viewmodels.AnalyticsUiState
import com.example.fintreker.ui.viewmodels.AnalyticsViewModel

/** Аналітика: розподіл витрат поточного місяця за категоріями. Клік по категорії відкриває історію. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onCategoryClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Аналітика · ${formatMonthTitle(viewModel.monthMillis)}") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val current = state) {
                AnalyticsUiState.Loading -> ShimmerBox(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(32.dp)
                        .size(220.dp),
                    shape = CircleShape
                )

                AnalyticsUiState.Empty -> Text(
                    "Цього місяця витрат ще немає.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                is AnalyticsUiState.Error -> Text(
                    current.message,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )

                is AnalyticsUiState.Success -> AnalyticsContent(current, onCategoryClick)
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    state: AnalyticsUiState.Success,
    onCategoryClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PieChart(
                    shares = state.shares,
                    modifier = Modifier.size(240.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Усього", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatAmount(state.total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        items(state.shares, key = { it.category.name }) { share ->
            LegendRow(share = share, onClick = { onCategoryClick(share.category.name) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun LegendRow(share: CategoryShare, onClick: () -> Unit) {
    val color = share.category.color()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${share.category.emoji} ${share.category.label}")
            LinearProgressIndicator(
                progress = { share.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(6.dp),
                color = color
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(formatAmount(share.total), fontWeight = FontWeight.SemiBold)
            Text(
                formatPercent(share.fraction.toDouble()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
