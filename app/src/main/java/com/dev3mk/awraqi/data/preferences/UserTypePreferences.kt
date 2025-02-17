package com.dev3mk.awraqi.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dev3mk.awraqi.util.Constants
import kotlinx.coroutines.flow.first

object UserTypePreferences {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATA_STORE_USER_TYPE)
    private val userTypeKey = stringPreferencesKey(Constants.USER_TYPE)

    suspend fun saveUserType(context: Context, email: String) {
        context.dataStore.edit { dataStore->
            dataStore[userTypeKey] = email
        }
    }

    suspend fun getUserType(context: Context): String? {
        context.dataStore.data.first().apply {
            return this[userTypeKey]
        }
    }
}