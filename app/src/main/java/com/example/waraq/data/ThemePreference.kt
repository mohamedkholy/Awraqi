package com.example.waraq.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.waraq.util.Constants
import kotlinx.coroutines.flow.first


object ThemePreference {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATA_STORE_NIGHT_MODE)
    private val themeKey = booleanPreferencesKey("night_mode")

    suspend fun setNightMode(context: Context, enableNightMode: Boolean) {
        context.dataStore.edit { dataStore->
            dataStore[themeKey] = enableNightMode
        }

    }

    suspend fun isNightModeEnabled(context: Context): Boolean {
        context.dataStore.data.first().apply {
            return this[themeKey]?:false
        }
    }
}