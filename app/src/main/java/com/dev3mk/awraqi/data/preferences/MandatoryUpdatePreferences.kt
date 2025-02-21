package com.dev3mk.awraqi.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dev3mk.awraqi.data.model.Version
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

object MandatoryUpdatePreferences {
    private const val FILE_NAME = "MandatoryUpdatePreferences_FILE_NAME"
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = FILE_NAME)
    private const val MANDATORY_UPDATE_PREFERENCES_KEY = "MANDATORY_UPDATE_PREFERENCES_KEY"
    private val key = stringPreferencesKey(MANDATORY_UPDATE_PREFERENCES_KEY)


    suspend fun saveVersionStatus(context: Context, version: Version) {
        context.dataStore.edit { dataStore ->
            dataStore[key] = Gson().toJson(version)
        }
    }

    suspend fun getMandatoryVersion(context: Context): Version? {
        context.dataStore.data.first().apply {
            val type = object : TypeToken<Version>(){}.type
            return  Gson().fromJson(this[key],type)
        }
    }


}