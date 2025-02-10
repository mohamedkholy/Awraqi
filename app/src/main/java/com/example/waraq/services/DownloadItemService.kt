package com.example.waraq.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.waraq.R
import com.example.waraq.data.model.DownloadState
import com.example.waraq.data.model.PaperItem
import com.example.waraq.data.MyRepository
import com.example.waraq.util.Constants
import com.example.waraq.util.CryptoManager
import com.example.waraq.util.FirebaseDownload
import com.example.waraq.data.preferences.LanguagePreference
import com.example.waraq.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class DownloadItemService : Service() {

    private lateinit var notificationManager: NotificationManager
    private val repository by inject<MyRepository>()
    private val firebaseDownload by inject<FirebaseDownload>()

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

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val item =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getSerializableExtra(Constants.SERVICE_INTENT_NAME, PaperItem::class.java)
            } else {
                intent?.getSerializableExtra(Constants.SERVICE_INTENT_NAME) as PaperItem
            }

        when (intent?.action) {
            Actions.START.toString() -> {
                if (item != null)
                    CoroutineScope(Dispatchers.IO).launch { startDownload(item) }
                else
                    stopSelf()
            }

            Actions.STOP.toString() -> stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)

    }


    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun startDownload(item: PaperItem) {
        startForeground(
            abs(item.hashCode()) + 1, getNotification(
                null,
                getString(R.string.downloading_items),
                item.title
            )
        )
        firebaseDownload.downloadBook(item).collect { result ->
            if (result is Int) {
                showNotification(
                    abs(item.id.hashCode()), getNotification(
                        result,
                        getString(R.string.is_downloading, item.title),
                        item.title
                    )
                )
            }

            when (result) {
                Constants.DOWNLOAD_FAILED -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@DownloadItemService,
                            getString(R.string.download_failed, item.title),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    removeNotification()
                    stopSelf()
                }

                Constants.DOWNLOAD_CANCELED -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@DownloadItemService,
                            getString(R.string.download_canceled, item.title),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    removeNotification()
                    stopSelf()
                }

                is File -> {
                    showNotification(
                        abs(item.id.hashCode()), getNotification(
                            null,
                            getString(R.string.is_downloaded, item.title),
                            item.title
                        )
                    )
                    val outFile = File(filesDir, item.title)
                    CryptoManager().encryptFile(result, FileOutputStream(outFile))
                    item.downloadState = DownloadState.downloaded
                    repository.saveItem(item)
                    stopSelf()
                }

                else -> {

                }
            }

        }
    }

    private fun removeNotification() {
        notificationManager.cancelAll()
    }

    private fun getNotification(
        progress: Int?,
        notificationTitle: String,
        itemTitle: String,
    ): Notification {
        val notification =
            NotificationCompat.Builder(this, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID).apply {
                setSmallIcon(R.drawable.baseline_auto_stories)
                setContentTitle(notificationTitle)
                if (progress != null) {
                    setContentTitle("$progress% - $notificationTitle")
                    setProgress(100, progress, false)
                    setOngoing(true)
                    sendBroadcast(Intent(itemTitle).apply {
                        setPackage(packageName)
                        putExtras(Bundle().apply {
                            putInt(Constants.DOWNLOAD_PROGRESS_KEY, progress)
                        })
                    })
                } else {
                    setPriority(NotificationManager.IMPORTANCE_HIGH)
                }
            }.build()

        return notification
    }

    private fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }


    enum class Actions {
        START, STOP
    }

}