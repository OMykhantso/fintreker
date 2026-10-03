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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fintreker.data.ExpenseStore
import com.example.fintreker.domain.CategoryShare
import com.example.fintreker.domain.CategoryTotal
import com.example.fintreker.domain.calculateShares
import com.example.fintreker.domain.formatAmount
import com.example.fintreker.domain.formatMonthTitle
import com.example.fintreker.domain.formatPercent
import com.example.fintreker.domain.monthRange
import com.example.fintreker.ui.theme.color

/** Аналітика: розподіл витрат поточного місяця за категоріями. Клік по категорії відкриває історію. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onCategoryClick: (String) -> Unit) {
    val now = remember { System.currentTimeMillis() }
    val month = remember { monthRange(now) }
    val shares = calculateShares(
        ExpenseStore.expenses
            .filter { it.timestamp >= month.startMillis && it.timestamp < month.endMillis }
            .groupBy { it.category }
            .map { (category, items) -> CategoryTotal(category, items.sumOf { it.amount }) }
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Аналітика · ${formatMonthTitle(now)}") }) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (shares.isEmpty()) {
                Text(
                    "Цього місяця витрат ще немає.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Усього за місяць", style = MaterialTheme.typography.labelLarge)
                            Text(
                                formatAmount(shares.sumOf { it.total }),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(shares, key = { it.category.name }) { share ->
                        LegendRow(share = share, onClick = { onCategoryClick(share.category.name) })
                        HorizontalDivider()
                    }
                }
            }
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
