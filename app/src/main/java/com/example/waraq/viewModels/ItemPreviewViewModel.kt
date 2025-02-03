package com.example.waraq.viewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.waraq.data.DownloadState
import com.example.waraq.data.PaperItem
import com.example.waraq.repository.MyRepository


class ItemPreviewViewModel(application: Application) : AndroidViewModel(application) {

    val myRepository = MyRepository(getContext())
    val _liveData = MutableLiveData<List<Bitmap>>()
    val liveData: LiveData<List<Bitmap>> = _liveData

    fun startDownloading(item: PaperItem) {
        myRepository.downloadItem(getContext(), item)
    }

    fun changeItemDownloadState(item: PaperItem) {
        myRepository.saveItem(item)
    }

    fun getItemDownloadState(id: String): LiveData<DownloadState> {
        return myRepository.getItemDownloadState(id)
    }

    fun stopDownloadTask(item: PaperItem) {
        myRepository.cancelDownloadTask(item)
    }

    private fun getContext(): Context {
        return getApplication<Application>().applicationContext
    }
}