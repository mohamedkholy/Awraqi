package com.example.waraq.util

import android.content.Context
import com.example.waraq.data.preferences.LanguagePreference
import java.util.Locale

object LocaleHelper {

    suspend fun updateLanguage(context: Context, language: String): Context {
        LanguagePreference.saveLanguage(context, language)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

}