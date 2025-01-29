package com.example.waraq.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waraq.data.ItemsFilter
import com.example.waraq.data.PaperItem
import com.example.waraq.repository.MyRepository
import kotlinx.coroutines.launch

class ItemsViewModel: ViewModel() {

    private val repository= MyRepository()
    private val _storeItemsLiveData = MutableLiveData<List<PaperItem>>()
    var storeItemsLiveData: LiveData<List<PaperItem>> = _storeItemsLiveData
    var downloadedItemsLiveData:LiveData<List<PaperItem>> = MutableLiveData()
    val itemsFilter = MutableLiveData(ItemsFilter.PURCHASED)
    val searchText = MutableLiveData<String>()


    private fun getAllStoreBooks(){
        viewModelScope.launch {
            _storeItemsLiveData.postValue( repository.getAllStoreItems())
        }
    }

    private fun getDownLoadedItems(){
        downloadedItemsLiveData = repository.getAllDownloadedItems()
    }



    init {
        getAllStoreBooks()
        getDownLoadedItems()
    }

}