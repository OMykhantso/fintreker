package com.example.fintreker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fintreker.domain.PIN_LENGTH
import com.example.fintreker.ui.viewmodels.PinUiState

private val keypadRows = listOf(
    listOf('1', '2', '3'),
    listOf('4', '5', '6'),
    listOf('7', '8', '9')
)

/** Екран блокування: введіть PIN-код (Лаб 3, безпечне сховище). */
@Composable
fun PinScreen(
    state: PinUiState,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔒 FinTreker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Введіть PIN-код", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.semantics {
                    contentDescription = "Введено ${state.enteredLength} з $PIN_LENGTH цифр"
                }
            ) {
                repeat(PIN_LENGTH) { index ->
                    val filled = index < state.enteredLength
                    Spacer(
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = if (state.showError) "Невірний PIN-код. Спробуйте ще раз" else " ",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keypadRows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { digit -> KeypadButton(digit.toString()) { onDigit(digit) } }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Spacer(Modifier.size(72.dp))
                    KeypadButton("0") { onDigit('0') }
                    TextButton(onClick = onBackspace, modifier = Modifier.size(72.dp)) {
                        Text("⌫", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall)
    }
}
