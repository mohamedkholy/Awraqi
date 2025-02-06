package com.example.waraq.util

import android.content.Context
import com.example.waraq.data.DownloadState
import com.example.waraq.data.PaperItem
import com.example.waraq.repository.MyRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File


class FirebaseDownload(val context: Context) {

    private val firebaseStorage = Firebase.storage
    private val repository = MyRepository()
    private var bytesTransferred = 1L
    private var lastBytesTransferred = 0L
    private lateinit var item: PaperItem
    private lateinit var cacheFile: File

    fun downloadBook(paperItem: PaperItem) = callbackFlow {
        item = paperItem
        saveItem(DownloadState.downloading)

        val fileReference = firebaseStorage.getReferenceFromUrl(paperItem.url)
        cacheFile = File.createTempFile("temp file", paperItem.title, context.cacheDir)
        val downloadTask = fileReference.getFile(cacheFile)


        addDownloadTask(downloadTask, this@callbackFlow)

        var timeOutRepeats = 0
        repeat(Int.MAX_VALUE) {
            delay(1000)
            if (getDownloadRate() <= 60) {
                timeOutRepeats++
            } else {
                timeOutRepeats = 0
            }
            if (timeOutRepeats > 60) {
                downloadTask.cancel()
                saveItem(DownloadState.notDownloded)
                send(Constants.DOWNLOAD_FAILED)
                delay(1000)
                cancel()
            }
        }


    }.flowOn(Dispatchers.IO)

    private fun addDownloadTask(downloadTask: FileDownloadTask, producerScope: ProducerScope<Any>) {
        val coverReference = firebaseStorage.getReferenceFromUrl(item.coverUrl)
        val coverFile = File(context.filesDir, item.title + "-cover")

        producerScope.apply {
            downloadTask.apply {
                addOnCompleteListener { fileDownloadTask ->
                    if (fileDownloadTask.isSuccessful) {
                        coverReference.getFile(coverFile)
                            .addOnCompleteListener { fileCoverDownloadTask ->
                                if (fileCoverDownloadTask.isSuccessful) {
                                    trySend(cacheFile)
                                    close()
                                } else {
                                    trySend(Constants.DOWNLOAD_FAILED)
                                    saveItem(DownloadState.notDownloded)
                                    close()
                                }
                            }
                    } else if (fileDownloadTask.isCanceled) {
                        trySend(Constants.DOWNLOAD_CANCELED)
                        close()
                    } else {
                        trySend(Constants.DOWNLOAD_FAILED)
                        saveItem(DownloadState.notDownloded)
                        close()
                    }
                }
                addOnProgressListener { taskSnapshot ->
                    bytesTransferred = taskSnapshot.bytesTransferred
                    val progress =
                        (100 * bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    trySend(progress)
                }
            }
        }
    }

    private fun getDownloadRate(): Long {
        val rate = (bytesTransferred - lastBytesTransferred) / 1000
        lastBytesTransferred = bytesTransferred
        return rate
    }

    private fun saveItem(downloadState: DownloadState) {
        item.apply {
            this.downloadState = downloadState
            repository.saveItem(this)
        }

    }


}



