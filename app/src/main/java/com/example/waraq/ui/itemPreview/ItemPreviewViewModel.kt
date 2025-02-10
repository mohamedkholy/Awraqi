package com.example.waraq.ui.itemPreview


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waraq.data.model.DownloadState
import com.example.waraq.data.model.PaperItem
import com.example.waraq.data.MyRepository
import kotlinx.coroutines.launch


class ItemPreviewViewModel(private val myRepository : MyRepository): ViewModel(){



    fun startDownloading(item: PaperItem) {
        myRepository.downloadItem(item)
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