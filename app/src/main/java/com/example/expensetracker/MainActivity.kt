package com.example.expensetracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.expensetracker.navigation.AppNavigation
import com.example.expensetracker.ui.screens.PinScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val container = (application as ExpenseTrackerApp).container
        setContent {
            ExpenseTrackerTheme {
                Root(container)
            }
        }
    }
}

@Composable
private fun Root(container: AppContainer) {
    // Якщо встановлено PIN — спочатку екран розблокування.
    var unlocked by rememberSaveable { mutableStateOf(!container.secure.hasPin()) }
    if (unlocked) {
        AppNavigation(container)
    } else {
        PinScreen(verify = container.secure::verifyPin, onUnlocked = { unlocked = true })
    }
}
