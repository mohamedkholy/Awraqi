package com.example.waraq.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.waraq.data.DownloadState
import com.example.waraq.data.PaperItem
import com.example.waraq.repository.MyRepository
import kotlinx.coroutines.launch


class ItemPreviewViewModel(application: Application) : AndroidViewModel(application) {

    private val myRepository = MyRepository(getContext())

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

    fun addFavoriteItem(id: String) {
         viewModelScope.launch {
             myRepository.addFavoriteItem(id)
         }
    }

    fun isFavorite(id: String): Boolean {
        return myRepository.isFavorite(id)
    }

    fun removeFavoriteItem(id: String) {
          viewModelScope.launch {
              myRepository.removeFavoriteItem(id)
          }
    }
}