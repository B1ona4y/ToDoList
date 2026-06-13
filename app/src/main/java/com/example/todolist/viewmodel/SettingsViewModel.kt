package com.example.todolist.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val KEY_DARK_MODE           = booleanPreferencesKey("dark_mode")
        val KEY_NOTIFICATION_SOUND  = booleanPreferencesKey("notification_sound")
        val KEY_DEFAULT_SORT        = stringPreferencesKey("default_sort")
    }

    val settings = dataStore.data.map { prefs ->
        AppSettings(
            darkMode          = prefs[KEY_DARK_MODE]          ?: false,
            notificationSound = prefs[KEY_NOTIFICATION_SOUND] ?: true,
            defaultSort       = prefs[KEY_DEFAULT_SORT]
                ?.let { SortOrder.valueOf(it) }
                ?: SortOrder.BY_PRIORITY
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    fun setNotificationSound(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { it[KEY_NOTIFICATION_SOUND] = enabled }
    }

    fun setDefaultSort(sort: SortOrder) = viewModelScope.launch {
        dataStore.edit { it[KEY_DEFAULT_SORT] = sort.name }
    }
}

data class AppSettings(
    val darkMode: Boolean          = false,
    val notificationSound: Boolean = true,
    val defaultSort: SortOrder     = SortOrder.BY_PRIORITY
)