package com.example.waraq

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.waraq.data.ThemePreference
import com.example.waraq.dataBase.MyDatabase
import com.example.waraq.databinding.ActivityMainBinding
import com.example.waraq.util.LanguagePreference
import com.example.waraq.util.LocaleHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { setNightMode() }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)
        MyDatabase.createInstance(this)
        requestNotificationPermission()
        setSecureFlags(window)
        preventMonitoring()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }

    private fun preventMonitoring() {
        //        CoroutineScope(Dispatchers.Default).launch {
//            while (true)
//            {
//                SecurityManager.preventScreenMonitoring(this@MainActivity)
//                delay(3000)
//            }
//        }
    }

    private suspend fun setNightMode() {
        val isNightModeEnabled = ThemePreference.isNightModeEnabled(this)
        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
           super.attachBaseContext(if (lang != null) LocaleHelper.updateLanguage(base, lang) else base)
       }
    }


}