package com.example.waraq.data

import android.content.Context
import com.example.waraq.util.Constants


object ThemePreference {
    private const val KEY_NIGHT_MODE = "night_mode"

    fun setNightMode(context: Context, enableNightMode: Boolean) {
        val prefs = context.getSharedPreferences(Constants.DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NIGHT_MODE, enableNightMode).apply()
    }

    fun isNightModeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NIGHT_MODE, false)
    }
}