package com.example.waraq.util

import android.content.Context
import android.content.SharedPreferences

object LanguagePreference {
    private const val KEY_LANGUAGE = "language"

    fun saveLanguage(context: Context, language: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(Constants.DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguage(context: Context): String? {
        val prefs: SharedPreferences =
            context.getSharedPreferences(Constants.DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null)
    }
}