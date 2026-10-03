package com.example.fintreker.ui.screens

import androidx.compose.runtime.mutableStateListOf
import com.example.fintreker.data.MockData
import com.example.fintreker.domain.Expense
import com.example.fintreker.ui.components.ExpenseRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fintreker.domain.AmountError
import com.example.fintreker.domain.AmountValidation
import com.example.fintreker.domain.Category
import com.example.fintreker.domain.validateAmount
import com.example.fintreker.ui.components.CategoryChips

/**
 * Лаб 1: стартовий екран додавання витрати.
 * Сума (`KeyboardType.Decimal`), категорія (`FilterChip`), кнопка збереження з валідацією (сума > 0).
 * Стан зберігається у `remember`, а список витрат — у пам'яті (mock-дані).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen() {
    var amountText by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf(Category.FOOD.name) }
    var note by rememberSaveable { mutableStateOf("") }
    var amountError by remember { mutableStateOf<AmountError?>(null) }
    val expenses = remember {
        mutableStateListOf(*MockData.generate(System.currentTimeMillis(), count = 5).toTypedArray())
    }
    val category = Category.fromNameOrOther(categoryName)
    val errorMessage = amountError?.message

    fun save() {
        when (val validation = validateAmount(amountText)) {
            is AmountValidation.Invalid -> amountError = validation.error
            is AmountValidation.Valid -> {
                expenses.add(
                    0,
                    Expense(
                        amount = validation.amount,
                        category = category.name,
                        timestamp = System.currentTimeMillis(),
                        note = note.trim()
                    )
                )
                amountText = ""
                note = ""
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Нова витрата") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { raw ->
                    amountText = raw.filter { it in '0'..'9' || it == '.' || it == ',' }
                    amountError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Сума") },
                suffix = { Text("₴") },
                singleLine = true,
                isError = amountError != null,
                supportingText = if (errorMessage != null) {
                    { Text(errorMessage) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text("Категорія", style = MaterialTheme.typography.titleMedium)
            CategoryChips(
                selected = category,
                onSelect = { categoryName = it.name }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(120) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Нотатка (необов'язково)") },
                maxLines = 2
            )

            Button(
                onClick = { save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Зберегти витрату")
            }

            Text("Останні витрати", style = MaterialTheme.typography.titleMedium)
            expenses.take(5).forEach { expense ->
                ExpenseRow(expense)
                HorizontalDivider()
            }
        }
    }
}
