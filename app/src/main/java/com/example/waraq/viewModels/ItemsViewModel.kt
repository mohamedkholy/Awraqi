package com.example.waraq.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.waraq.data.ItemsFilter
import com.example.waraq.data.PaperItem
import com.example.waraq.repository.MyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemsViewModel(application: Application): AndroidViewModel(application) {

    private val repository= MyRepository(getAppContext())
    private val _storeItemsLiveData = MutableLiveData<List<PaperItem>>()
    var storeItemsLiveData: LiveData<List<PaperItem>> = _storeItemsLiveData
    var downloadedItemsLiveData:LiveData<List<PaperItem>> = MutableLiveData()
    val itemsFilter = MutableLiveData(ItemsFilter.PURCHASED)
    val searchText = MutableLiveData<String>()


     fun getStoreBooks(){
        viewModelScope.launch {
            _storeItemsLiveData.postValue( repository.getAllStoreItems())
        }
    }


    private fun getDownLoadedItems(){
        downloadedItemsLiveData = repository.getAllDownloadedItems()
    }

    suspend fun getUserBooksIds(): List<String> {
       return withContext(Dispatchers.Default){
           return@withContext repository.getUserBooksIds()
       }
    }


    fun updateItem(item: PaperItem)
    { viewModelScope.launch {
            repository.saveItem(item)
        }
    }



    private fun getAppContext(): Context {
        return getApplication<Application>().applicationContext
    }

    fun getFavoriteItems(): List<String> {
       return repository.getFavoriteItems()
    }

     suspend fun saveFavoriteItems(email: String): Boolean {
         return repository.saveFavoriteItems(email)
    }

    fun deleteItem(item: PaperItem) {
         viewModelScope.launch {
             repository.deleteItem(item)
         }
    }


    init {
        getStoreBooks()
        getDownLoadedItems()
    }

}