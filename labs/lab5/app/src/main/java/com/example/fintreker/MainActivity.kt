package com.example.fintreker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fintreker.navigation.AppNavigation
import com.example.fintreker.ui.components.RequestNotificationPermission
import com.example.fintreker.ui.screens.PinScreen
import com.example.fintreker.ui.theme.FinTrekerTheme
import com.example.fintreker.ui.viewmodels.AppViewModelFactory
import com.example.fintreker.ui.viewmodels.PinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = AppViewModelFactory((application as FinTrekerApp).container)

        setContent {
            FinTrekerTheme {
                val pinViewModel: PinViewModel = viewModel(factory = factory)
                val pinState by pinViewModel.uiState.collectAsStateWithLifecycle()

                if (pinState.isLocked) {
                    PinScreen(
                        state = pinState,
                        onDigit = pinViewModel::onDigit,
                        onBackspace = pinViewModel::onBackspace
                    )
                } else {
                    RequestNotificationPermission()
                    AppNavigation(factory = factory)
                }
            }
        }
    }
}
