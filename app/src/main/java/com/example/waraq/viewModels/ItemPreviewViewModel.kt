package com.example.waraq.viewModels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.waraq.data.Downloaded
import com.example.waraq.data.PaperItem
import com.example.waraq.repository.MyRepository


class ItemPreviewViewModel(application: Application) : AndroidViewModel(application) {

    val myRepository = MyRepository()
    val _liveData = MutableLiveData<List<Bitmap>>()
    val liveData: LiveData<List<Bitmap>> = _liveData

    fun startDownloading(item: PaperItem) {
        val context = getApplication<Application>().applicationContext
        myRepository.downloadItem(context, item)
    }

    fun changeItemDownloadState(item: PaperItem) {
        myRepository.saveItem(item)
    }

    fun getItemDownloadState(id: String): LiveData<Downloaded> {
        return MyRepository().getItemDownloadState(id)
    }

    fun stopDownloadTask(item: PaperItem) {
        myRepository.cancelDownloadTask(item)
    }
}