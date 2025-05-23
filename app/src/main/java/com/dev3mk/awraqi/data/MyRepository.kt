package com.dev3mk.awraqi.data

import androidx.lifecycle.LiveData
import com.dev3mk.awraqi.data.dataSources.LocalDataSource
import com.dev3mk.awraqi.data.dataSources.RemoteDataSource
import com.dev3mk.awraqi.data.model.DownloadState
import com.dev3mk.awraqi.data.model.DrawingItem
import com.dev3mk.awraqi.data.model.Notes
import com.dev3mk.awraqi.data.model.PageNotes
import com.dev3mk.awraqi.data.model.PaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) {

    fun downloadItem(item: PaperItem) {
        remoteDataSource.downloadItem(item)
    }

    suspend fun getAllStoreItems(): List<PaperItem> {
        return withContext(Dispatchers.IO) {
            remoteDataSource.getAllStoreItems()
        }
    }

    suspend fun getUserBooksIds(): List<String> {
        return remoteDataSource.getUserBooksIds()
    }


    fun cancelDownloadTask(item: PaperItem) {
        remoteDataSource.cancelDownloadTask(item)
        saveItem(item)
    }

    suspend fun addFavoriteItem(id: String) {
        localDataSource.addFavoriteItem(id)
        remoteDataSource.addToFireStore(id)
    }

    suspend fun removeFavoriteItem(id: String) {
        localDataSource.removeFavoriteItem(id)
        remoteDataSource.removeFavoriteItem(id)

    }

    suspend fun saveFavoriteItems(email: String) {
        val favoriteIds = remoteDataSource.getFavoriteItems(email)
        localDataSource.saveFavoriteItems(favoriteIds)
    }

    fun getAllDownloadedItems(): LiveData<List<PaperItem>> {
        return localDataSource.getAllDownloadedItems()
    }

    fun saveItem(item: PaperItem) {
        localDataSource.saveItem(item)
    }

    fun getItemDownloadState(id: String): LiveData<DownloadState> {
        return localDataSource.getItemDownloadState(id)
    }

    fun getFavoriteItems(): List<String> {
        return localDataSource.getFavoriteItems()
    }

    fun deleteItem(item: PaperItem) {
        localDataSource.deleteItem(item)
    }

    suspend fun getDrawingItem(id: String): DrawingItem? {
        return localDataSource.getDrawingItem(id)
    }

    fun upsertDrawingItem(drawingItem: DrawingItem) {
        localDataSource.upsertDrawingItem(drawingItem)
    }

    fun getAllNotes(pdfId: String): Notes {
        return localDataSource.getAllNotes(pdfId)
    }

    suspend fun saveNote(pageNote: PageNotes) {
        localDataSource.saveNote(pageNote)
    }

    fun isFavorite(id: String): Boolean {
        return localDataSource.isFavorite(id)
    }


}


