package com.example.waraq.ui.homeItems


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waraq.data.MyRepository
import com.example.waraq.data.model.ItemsFilter
import com.example.waraq.data.model.PaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemsViewModel(private val myRepository : MyRepository): ViewModel(){
    
    private val _storeItemsLiveData = MutableLiveData<List<PaperItem>>()
    var storeItemsLiveData: LiveData<List<PaperItem>> = _storeItemsLiveData
    var downloadedItemsLiveData:LiveData<List<PaperItem>> = MutableLiveData()
    val itemsFilter = MutableLiveData(ItemsFilter.PURCHASED)
    val searchText = MutableLiveData<String>()


     fun getStoreBooks(){
        viewModelScope.launch {
            _storeItemsLiveData.postValue( myRepository.getAllStoreItems())
        }
    }


    private fun getDownLoadedItems(){
        downloadedItemsLiveData = myRepository.getAllDownloadedItems()
    }

    suspend fun getUserBooksIds(): List<String> {
       return withContext(Dispatchers.Default){
           return@withContext myRepository.getUserBooksIds()
       }
    }


    fun updateItem(item: PaperItem)
    { viewModelScope.launch {
            myRepository.saveItem(item)
        }
    }
    

    fun getFavoriteItems(): List<String> {
       return myRepository.getFavoriteItems()
    }

     suspend fun saveFavoriteItems(email: String) {
          myRepository.saveFavoriteItems(email)
    }

    fun deleteItem(item: PaperItem) {
         viewModelScope.launch {
             myRepository.deleteItem(item)
         }
    }


    init {
        getStoreBooks()
        getDownLoadedItems()
    }

}