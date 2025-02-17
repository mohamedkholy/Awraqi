package com.dev3mk.awraqi.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dev3mk.awraqi.util.Constants
import kotlinx.coroutines.flow.first

object EmailPreferences {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATA_STORE_EMAIL)
    private val emailKey = stringPreferencesKey(Constants.EMAIL_KEY)

    suspend fun saveEmail(context: Context, email: String?) {
        context.dataStore.edit { dataStore->
           if (email==null) dataStore.clear() else dataStore[emailKey] = email
        }
    }

    suspend fun getEmail(context: Context): String? {
        context.dataStore.data.first().apply {
            return this[emailKey]
        }
    }


}