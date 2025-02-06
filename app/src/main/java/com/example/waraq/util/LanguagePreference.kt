package com.example.waraq.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


object LanguagePreference {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATA_STORE_LANGUAGE)
    private val languageKey = stringPreferencesKey("language")



    suspend fun saveLanguage(context: Context, language: String) {
        context.dataStore.edit { dataStore->
            dataStore[languageKey] = language
        }
    }

    suspend fun getLanguage(context: Context): String? {
       context.dataStore.data.first().apply {
           return this[languageKey]
       }
    }

}