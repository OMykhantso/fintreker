@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.domain.Category
import com.example.expensetracker.domain.Currency
import com.example.expensetracker.ui.components.CategoryPieChart
import com.example.expensetracker.ui.components.Format
import com.example.expensetracker.ui.components.SectionTitle
import com.example.expensetracker.ui.viewmodels.AddExpenseViewModel
import com.example.expensetracker.ui.viewmodels.HistoryViewModel
import com.example.expensetracker.ui.viewmodels.HomeUiState
import com.example.expensetracker.ui.viewmodels.HomeViewModel
import com.example.expensetracker.ui.viewmodels.SettingsViewModel
import com.example.expensetracker.work.ReminderScheduler

// ---------------------------------------------------------------- Home

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddExpense: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Трекер витрат") },
                actions = {
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, "Налаштування") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) { Icon(Icons.Filled.Add, "Додати витрату") }
        }
    ) { padding ->
        when (val s = state) {
            HomeUiState.Loading -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }

            is HomeUiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { BalanceCard(s, viewModel::selectCurrency) }
                item { RatesLine(s) }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Останні витрати")
                        TextButton(onClick = onOpenHistory) { Text("Усі →") }
                    }
                }
                if (s.recent.isEmpty()) {
                    item {
                        Text(
                            "Ще немає витрат. Натисніть «+», щоб додати.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(s.recent, key = { it.id }) { ExpenseRow(it, onDelete = null) }
            }
        }
    }
}

@Composable
private fun BalanceCard(s: HomeUiState.Success, onCurrency: (Currency) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Currency.entries.forEach { c ->
                    FilterChip(selected = s.currency == c, onClick = { onCurrency(c) }, label = { Text(c.code) })
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Залишок бюджету на місяць", style = MaterialTheme.typography.labelLarge)
            val converted = s.remainingConverted
            Text(
                text = if (converted != null) Format.money(converted, s.currency) else "Курс недоступний",
                fontSize = 34.sp,
                color = if (s.remaining < 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text(
                "Витрачено ${Format.money(s.spent)} з ${Format.money(s.limit)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (s.remaining < 0) {
                Text(
                    "⚠ Ліміт бюджету перевищено",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RatesLine(s: HomeUiState.Success) {
    val usd = s.rates["USD"]
    val eur = s.rates["EUR"]
    val text = if (usd == null && eur == null) {
        "Курси НБУ ще не завантажено (офлайн)"
    } else {
        "НБУ: USD ${usd?.let { "%.2f".format(it) } ?: "—"} · EUR ${eur?.let { "%.2f".format(it) } ?: "—"}" +
            (s.ratesUpdatedAt?.let { " · оновлено ${Format.date(it)}" } ?: "")
    }
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ExpenseRow(expense: ExpenseEntity, onDelete: (() -> Unit)?) {
    val cat = Category.fromNameOrOther(expense.category)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(cat.emoji, fontSize = 24.sp)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(cat.label, style = MaterialTheme.typography.titleSmall)
                val sub = listOf(expense.note, Format.date(expense.timestamp))
                    .filter { it.isNotBlank() }.joinToString(" · ")
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("−${Format.money(expense.amount)}", style = MaterialTheme.typography.titleSmall, color = cat.color)
            if (onDelete != null) {
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Видалити витрату") }
            }
        }
    }
}

// ---------------------------------------------------------------- Add expense

@Composable
fun AddExpenseScreen(viewModel: AddExpenseViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.form
    LaunchedEffect(form.saved) { if (form.saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Нова витрата") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = form.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Сума, ₴") },
                isError = form.error != null,
                supportingText = { form.error?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Категорія", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    FilterChip(
                        selected = form.category == c,
                        onClick = { viewModel.onCategoryChange(c) },
                        label = { Text("${c.emoji} ${c.label}") }
                    )
                }
            }
            OutlinedTextField(
                value = form.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Нотатка (необов'язково)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Залишок бюджету: ${Format.money(state.remaining)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.willExceedBudget) {
                Text("⚠ Ця витрата перевищить місячний ліміт", color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Зберегти витрату") }
        }
    }
}

// ---------------------------------------------------------------- History / analytics

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    InfiniteScrollEffect(listState, enabled = state.hasMore, onLoadMore = viewModel::loadMore)

    Scaffold(topBar = { TopAppBar(title = { Text("Аналітика та історія") }) }) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SectionTitle("Розподіл за поточний місяць")
                CategoryPieChart(state.monthTotals)
            }
            item {
                SectionTitle("Історія")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.category == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Усі") }
                    )
                    Category.entries.forEach { c ->
                        FilterChip(
                            selected = state.category == c,
                            onClick = { viewModel.selectCategory(c) },
                            label = { Text("${c.emoji} ${c.label}") }
                        )
                    }
                }
                Text(
                    "Разом: ${Format.money(state.total)}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.items.isEmpty()) {
                item { Text("Немає записів", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(state.items, key = { it.id }) { e -> ExpenseRow(e, onDelete = { viewModel.delete(e) }) }
        }
    }
}

/** Infinite Scroll: коли видно останні елементи — підвантажуємо наступну порцію. */
@Composable
private fun InfiniteScrollEffect(listState: LazyListState, enabled: Boolean, onLoadMore: () -> Unit) {
    LaunchedEffect(listState, enabled) {
        if (!enabled) return@LaunchedEffect
        snapshotFlow {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: 0) >= info.totalItemsCount - 3
        }.collect { nearEnd -> if (nearEnd) onLoadMore() }
    }
}

// ---------------------------------------------------------------- Settings

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var limitText by remember(state.limit) { mutableStateOf(state.limit.toLong().toString()) }
    var pinText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Налаштування") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle("Місячний бюджет (зберігається зашифровано)")
            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it },
                label = { Text("Ліміт, ₴") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.saveLimit(limitText) }) { Text("Зберегти ліміт") }

            SectionTitle("PIN-код")
            if (state.hasPin) {
                OutlinedButton(onClick = viewModel::clearPin) { Text("Вимкнути PIN") }
            } else {
                OutlinedTextField(
                    value = pinText,
                    onValueChange = { if (it.length <= 4) pinText = it.filter(Char::isDigit) },
                    label = { Text("Новий PIN (4 цифри)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { viewModel.setPin(pinText); pinText = "" }) { Text("Встановити PIN") }
            }

            SectionTitle("Дані та сповіщення")
            OutlinedButton(onClick = viewModel::refreshRates) { Text("Оновити курси НБУ") }
            state.ratesUpdatedAt?.let {
                Text("Курси оновлено: ${Format.date(it)}", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { ReminderScheduler.triggerNow(context) }) { Text("Тестове нагадування зараз") }
            OutlinedButton(onClick = viewModel::seedDemoData) { Text("Додати демо-дані") }

            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

// ---------------------------------------------------------------- PIN gate

@Composable
fun PinScreen(verify: (String) -> Boolean, onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔒 Введіть PIN", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 4 && it.all(Char::isDigit)) {
                    pin = it
                    error = false
                    if (it.length == 4) {
                        if (verify(it)) onUnlocked() else { error = true; pin = "" }
                    }
                }
            },
            label = { Text("PIN") },
            isError = error,
            supportingText = { if (error) Text("Невірний PIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
    }
}
