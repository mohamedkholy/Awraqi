package com.example.waraq.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.waraq.R
import com.example.waraq.data.Downloaded
import com.example.waraq.data.PaperItem
import com.example.waraq.repository.MyRepository
import com.example.waraq.util.Constants
import com.example.waraq.util.CryptoManager
import com.example.waraq.util.FirebaseDownload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class DownloadItemService : Service() {

    private lateinit var notificationManager: NotificationManager

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

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

    private suspend fun startDownload(item: PaperItem) {

        startForeground(abs(item.hashCode())+1, getNotification(null, "downloading items"))
        FirebaseDownload(this).downloadBook(item).collect { result ->
            if (result is Int) {
                showNotification(abs(item.id.hashCode()), getNotification(result, "${item.title!!} is downloading"))
            }

            when (result) {
                Constants.DOWNLOAD_FAILED -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@DownloadItemService,
                            "${item.title} download failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                Constants.DOWNLOAD_CANCELED -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@DownloadItemService,
                            "${item.title} download canceled",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                is File -> {
                    showNotification(abs(item.id.hashCode()),getNotification( null, "${item.title!!} is downloaded"))
                    val outFile = File(filesDir, item.title)
                    CryptoManager().encryptFile(result, FileOutputStream(outFile))
                    item.downloadState = Downloaded.downloaded
                    MyRepository().saveItem(item)
                    stopSelf()
                }

                else -> {

                }
            }

        }
    }

    fun getNotification(progress: Int?, title: String): Notification {

        val notification =
            NotificationCompat.Builder(this, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID).apply {
                setSmallIcon(R.drawable.baseline_auto_stories)
                setContentTitle(title)
                if (progress != null) {
                    setProgress(100, progress, false)
                    setOngoing(true)
                }
                else{
                    setPriority(NotificationManager.IMPORTANCE_HIGH)
                }
            }.build()

        return notification
    }

    fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }


    enum class Actions {
        START, STOP
    }

}