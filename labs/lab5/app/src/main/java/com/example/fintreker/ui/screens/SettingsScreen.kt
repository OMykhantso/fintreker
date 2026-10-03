package com.example.fintreker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fintreker.domain.PIN_LENGTH
import com.example.fintreker.domain.REMINDER_HOUR
import com.example.fintreker.ui.viewmodels.SettingsViewModel

/** Налаштування: ліміт бюджету, PIN (EncryptedSharedPreferences), нагадування (WorkManager), демо-дані. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var budgetText by rememberSaveable(state.budgetLimit) { mutableStateOf(budgetToInput(state.budgetLimit)) }
    var pinText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissMessage()
        }
    }
    LaunchedEffect(state.pinEnabled) { pinText = "" }

    val budgetError = state.budgetError
    val pinError = state.pinError

    Scaffold(
        topBar = { TopAppBar(title = { Text("Налаштування") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = "Місячний бюджет") {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c in '0'..'9' || c == '.' || c == ',' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ліміт на місяць") },
                    suffix = { Text("₴") },
                    singleLine = true,
                    isError = budgetError != null,
                    supportingText = if (budgetError != null) {
                        { Text(budgetError.message) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.saveBudget(budgetText) }) { Text("Зберегти") }
                    if (state.budgetLimit > 0.0) {
                        TextButton(onClick = viewModel::clearBudget) { Text("Вимкнути ліміт") }
                    }
                }
                Text(
                    "Ліміт зберігається у зашифрованому сховищі (AES-256).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SettingsSection(title = "Безпека") {
                if (state.pinEnabled) {
                    Text("PIN-код встановлено. Додаток запитуватиме його при кожному запуску.")
                    OutlinedButton(onClick = viewModel::disablePin) { Text("Вимкнути PIN") }
                } else {
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { pinText = it.filter { c -> c in '0'..'9' }.take(PIN_LENGTH) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Новий PIN ($PIN_LENGTH цифри)") },
                        singleLine = true,
                        isError = pinError != null,
                        supportingText = if (pinError != null) {
                            { Text(pinError) }
                        } else {
                            null
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    Button(onClick = { viewModel.setPin(pinText) }) { Text("Встановити PIN") }
                }
                Text(
                    "PIN зберігається лише як SHA-256 хеш із сіллю.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SettingsSection(title = "Нагадування") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Щодня о $REMINDER_HOUR:00: «Не забудьте зафіксувати сьогоднішні витрати!»",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = viewModel::setReminderEnabled
                    )
                }
                OutlinedButton(onClick = viewModel::sendTestReminder) { Text("Тестове нагадування зараз") }
            }

            SettingsSection(title = "Дані") {
                Button(onClick = viewModel::addDemoData) { Text("Додати демо-витрати") }
                OutlinedButton(onClick = viewModel::clearExpenses) { Text("Видалити всі витрати") }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** 5000.0 -> «5000», 5000.5 -> «5000.5», 0 -> «» (порожнє поле). */
private fun budgetToInput(value: Double): String = when {
    value <= 0.0 -> ""
    value % 1.0 == 0.0 -> value.toLong().toString()
    else -> value.toString()
}
