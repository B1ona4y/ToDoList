package com.example.todolist.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_LOCALIZED_NOTIFICATIONS = booleanPreferencesKey("localized_notifications")
    }

    val settings = dataStore.data.map { prefs ->
        AppSettings(
            darkMode = prefs[KEY_DARK_MODE]               ?: false,
            notificationsEnabled = prefs[KEY_NOTIFICATIONS_ENABLED]   ?: true,
            localizedNotifications = prefs[KEY_LOCALIZED_NOTIFICATIONS] ?: false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
        if (!enabled) dataStore.edit { it[KEY_LOCALIZED_NOTIFICATIONS] = false }
    }

    fun setLocalizedNotifications(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[KEY_LOCALIZED_NOTIFICATIONS] = enabled }
    }
}

data class AppSettings(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val localizedNotifications: Boolean = false,
)
