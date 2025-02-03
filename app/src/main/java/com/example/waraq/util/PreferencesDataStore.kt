package com.example.waraq.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore



object PreferencesDataStore {
    private const val PREFERENCE_NAME = "app_preferences"

    // Define a DataStore instance
    private var dataStore: DataStore<Preferences>? = null

    // Get DataStore instance
    fun getInstance(context: Context): DataStore<Preferences> {

        return dataStore!!
    }
}