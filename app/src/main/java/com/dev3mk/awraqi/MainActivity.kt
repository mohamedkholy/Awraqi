package com.dev3mk.awraqi

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.dev3mk.awraqi.data.preferences.ThemePreference
import com.dev3mk.awraqi.databinding.ActivityMainBinding
import com.dev3mk.awraqi.data.preferences.LanguagePreference
import com.dev3mk.awraqi.ui.splash.SplashViewModel
import com.dev3mk.awraqi.util.LocaleHelper
import com.dev3mk.awraqi.util.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.splashScreenFlag.value!!
            }
        }

        setNightMode()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)
        setSecureFlags(window)
        preventMonitoring()
    }

    private fun preventMonitoring() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                SecurityManager.preventScreenMonitoring(applicationContext)
                delay(3000)
            }
        }
    }

    private fun setNightMode() {
       lifecycleScope.launch {
           val isNightModeEnabled = ThemePreference.isNightModeEnabled(this@MainActivity)
           if (isNightModeEnabled == null) {
               AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
           } else if (isNightModeEnabled) {
               AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
           } else {
               AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
           }
       }
    }

    private fun setSecureFlags(window: Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

    }

    override fun attachBaseContext(base: Context) {
        runBlocking {
            val lang = LanguagePreference.getLanguage(base)
            super.attachBaseContext(
                if (lang != null) LocaleHelper.updateLanguage(
                    base,
                    lang
                ) else base
            )
        }
    }



}