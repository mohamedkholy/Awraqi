package com.dev3mk.awraqi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.dev3mk.awraqi.di.databaseModule
import com.dev3mk.awraqi.di.dataModule
import com.dev3mk.awraqi.di.viewModelModule
import com.dev3mk.awraqi.util.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        prepareKoin()
    }

    private fun prepareKoin() {
        startKoin {
            androidContext(this@MyApp)
            modules(dataModule, viewModelModule, databaseModule)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                Constants.DOWNLOAD_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setSound(null, null)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }


}