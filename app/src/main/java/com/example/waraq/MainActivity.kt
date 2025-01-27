package com.example.waraq

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.waraq.dataBase.MyDatabase
import com.example.waraq.databinding.ActivityMainBinding
import com.example.waraq.util.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        MyDatabase.createInstance(this)
        setSecureFlags(window)
//        CoroutineScope(Dispatchers.Default).launch {
//            while (true)
//            {
//                SecurityManager.preventScreenMonitoring(this@MainActivity)
//                delay(3000)
//            }
//        }
    }

    private fun setSecureFlags(window: Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

    }


}