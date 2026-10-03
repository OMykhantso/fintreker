package com.example.fintreker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fintreker.AppContainer

/** Проста ручна DI: фабрика збирає ViewModel із залежностей [AppContainer]. */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(container.expenseRepository, container.secureStore)

        modelClass.isAssignableFrom(RatesViewModel::class.java) ->
            RatesViewModel(container.ratesRepository)

        modelClass.isAssignableFrom(AddExpenseViewModel::class.java) ->
            AddExpenseViewModel(container.expenseRepository, container.secureStore)

        modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
            HistoryViewModel(container.expenseRepository)

        modelClass.isAssignableFrom(AnalyticsViewModel::class.java) ->
            AnalyticsViewModel(container.expenseRepository)

        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(
                container.secureStore,
                container.expenseRepository,
                container.reminderController
            )

        modelClass.isAssignableFrom(PinViewModel::class.java) ->
            PinViewModel(container.secureStore)

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T
}
