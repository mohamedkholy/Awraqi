package com.example.waraq.data.dataSources

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.waraq.data.dataBase.MyDatabase
import com.example.waraq.data.model.DownloadState
import com.example.waraq.data.model.DrawingItem
import com.example.waraq.data.model.ItemId
import com.example.waraq.data.model.Notes
import com.example.waraq.data.model.PageNotes
import com.example.waraq.data.model.PaperItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LocalDataSource(val context: Context, myDatabase: MyDatabase) {

    private val dao = myDatabase.dao

    fun getAllDownloadedItems(): LiveData<List<PaperItem>> {
        return dao.getAllItems()
    }

    fun saveItem(paperItem: PaperItem) {
        CoroutineScope(Dispatchers.Main).launch {
            dao.upsertItem(paperItem)
        }
    }


    fun getItemDownloadState(id: String): LiveData<DownloadState> {
        return dao.getItemDownloadState(id)
    }

    suspend fun getDrawingItem(id: String): DrawingItem? {
        return withContext(Dispatchers.IO) {
            dao.getDrawingItem(id)
        }
    }

    fun upsertDrawingItem(drawingItem: DrawingItem) {
        CoroutineScope(Dispatchers.Default).launch {
            dao.upsertDrawingItem(drawingItem)
        }
    }

    fun getAllNotes(pdfId: String): Notes {
        return dao.selectFileNotes(pdfId)
    }

    suspend fun saveNote(pageNote: PageNotes) {
        dao.upsertPageNote(pageNote)
    }

    fun addFavoriteItem(id: String) {
        dao.addFavoriteItem(ItemId(id))
    }


    fun isFavorite(id: String): Boolean {
        return dao.isFavorite(id)
    }

    fun getFavoriteItems(): List<String> {
        return dao.getFavoriteItems()
    }

    fun deleteItem(item: PaperItem) {
        if (deleteItemFiles(item)) {
            dao.deleteItem(item)
        }
    }


    private fun deleteItemFiles(item: PaperItem): Boolean {
        var deletePdfSuccess = true
        val pdfFile = File(context.filesDir, item.title)
        val coverFile = File(context.filesDir, item.title + "-cover")
        if (pdfFile.exists()) {
            deletePdfSuccess = pdfFile.delete()
        }
        if (coverFile.exists() && deletePdfSuccess) {
            coverFile.delete()
        }

        return deletePdfSuccess
    }

    fun removeFavoriteItem(id: String) {
        dao.removeFavoriteItem(id)
    }

    fun saveFavoriteItems(favoriteIds: List<ItemId>) {
        favoriteIds.forEach { item ->
            dao.addFavoriteItem(item)
        }
    }


}