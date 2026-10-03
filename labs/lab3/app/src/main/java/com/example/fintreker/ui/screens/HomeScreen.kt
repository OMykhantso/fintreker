package com.example.fintreker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fintreker.domain.Balance
import com.example.fintreker.domain.BudgetStatus
import com.example.fintreker.domain.formatAmount
import com.example.fintreker.domain.formatMonthTitle
import com.example.fintreker.domain.formatPercent
import com.example.fintreker.ui.components.ExpenseRow
import com.example.fintreker.ui.theme.color
import com.example.fintreker.ui.viewmodels.HomeUiState
import com.example.fintreker.ui.viewmodels.HomeViewModel

/** Головний екран: баланс місячного бюджету (ліміт із EncryptedSharedPreferences, витрати з Room). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onAddExpense: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("FinTreker") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Додати витрату") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = homeState) {
                HomeUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Error -> item {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }

                is HomeUiState.Success -> {
                    item {
                        BalanceCard(
                            balance = state.balance,
                            monthTitle = formatMonthTitle(homeViewModel.monthMillis)
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Останні витрати", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = onOpenHistory) { Text("Уся історія") }
                        }
                    }
                    if (state.recent.isEmpty()) {
                        item {
                            Text(
                                "Витрат ще немає. Натисніть «Додати витрату» або додайте демо-дані в налаштуваннях.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(state.recent, key = { it.id }) { expense ->
                            ExpenseRow(expense)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Balance, monthTitle: String) {
    val statusColor = balance.status.color()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(monthTitle, style = MaterialTheme.typography.labelLarge)

            if (balance.status == BudgetStatus.NO_LIMIT) {
                Text("Витрачено цього місяця", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatAmount(balance.spent),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ліміт не задано — встановіть його в налаштуваннях, щоб бачити залишок.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("Залишок бюджету", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatAmount(balance.remaining),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (balance.status == BudgetStatus.EXCEEDED) statusColor else Color.Unspecified
                )
                LinearProgressIndicator(
                    progress = { balance.usedFraction.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = statusColor
                )
                Text(
                    "Витрачено ${formatAmount(balance.spent)} з ${formatAmount(balance.budget)} " +
                        "(${formatPercent(balance.usedFraction)})",
                    style = MaterialTheme.typography.bodySmall
                )
                when (balance.status) {
                    BudgetStatus.WARNING -> Text(
                        "⚠️ Ви використали понад 80% місячного ліміту",
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    BudgetStatus.EXCEEDED -> Text(
                        "⛔ Ліміт перевищено на ${formatAmount(-balance.remaining)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    else -> Unit
                }
            }
        }
    }
}
