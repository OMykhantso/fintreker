package com.example.fintreker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fintreker.ui.screens.AddExpenseScreen
import com.example.fintreker.ui.theme.FinTrekerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinTrekerTheme {
                AddExpenseScreen()
            }
        }
    }
}
