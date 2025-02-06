package com.example.waraq.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.example.waraq.dataBase.MyDatabase
import com.example.waraq.data.DownloadState
import com.example.waraq.data.DrawingItem
import com.example.waraq.data.ItemId
import com.example.waraq.data.Notes
import com.example.waraq.data.PageNotes
import com.example.waraq.data.PaperItem
import com.example.waraq.services.DownloadItemService
import com.example.waraq.util.Constants
import com.example.waraq.util.EmailPreferences
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class MyRepository(val context: Context? = null) {

    private val firestoreDatabase = Firebase.firestore
    private val dao = MyDatabase.createInstance().dao

    suspend fun getAllStoreItems(): List<PaperItem> {
        return withContext(Dispatchers.IO) {
            var paperItemList: List<PaperItem> = listOf(PaperItem())
            val userItemIdList = getUserBooksIds()
            val task = firestoreDatabase.collection(Constants.FIRE_STORE_BOOKS_COLLECTION)
                .get().await()
            if (task != null) {
                paperItemList = task.toObjects(PaperItem::class.java)
            }
            if (userItemIdList.isNotEmpty()) {
                paperItemList.forEach { item ->
                    if (userItemIdList.contains(item.id))
                        item.isPurchased = true
                }
            }
            return@withContext paperItemList
        }
    }

    suspend fun getUserBooksIds(): List<String> {
        val userEmail = EmailPreferences.getEmail(context!!)
        var booksIdsList = mutableListOf<ItemId>()
        if (userEmail == null)
            return mutableListOf()
        val bookIds =
            firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(userEmail)
                .collection(Constants.FIRE_STORE_USER_BOOKS_COLLECTION).get().await()
        if (!bookIds.isEmpty) {
            booksIdsList = bookIds.toObjects(ItemId::class.java)
        }
        return booksIdsList.map { it.id }
    }

    fun getAllDownloadedItems(): LiveData<List<PaperItem>> {
        return dao.getAllItems()

    }

    fun saveItem(paperItem: PaperItem) {
        CoroutineScope(Dispatchers.Main).launch {
            dao.upsertItem(paperItem)
        }
    }

    fun downloadItem(context: Context?, item: PaperItem) {
        if (context != null) {
            val intent = Intent(context, DownloadItemService::class.java)
            intent.setAction(DownloadItemService.Actions.START.toString())
            intent.putExtra(Constants.SERVICE_INTENT_NAME, item)
            context.startService(intent)
        }

    }

    fun cancelDownloadTask(item: PaperItem) {
        Firebase.storage.getReferenceFromUrl(item.url).activeDownloadTasks.forEach {
            it.cancel()
        }
        item.downloadState = DownloadState.notDownloded
        saveItem(item)
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

    suspend fun addFavoriteItem(id: String) {
        dao.addFavoriteItem(ItemId(id))
        addToFireStore(id)
    }

    private suspend fun addToFireStore(id: String) {
        val userEmail = EmailPreferences.getEmail(context!!)
        userEmail?.apply {
            firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(userEmail)
                .collection(Constants.USER_FAVORITES_COLLECTION).document(id)
                .set(ItemId(id))
        }

    }

    fun isFavorite(id: String): Boolean {
        return dao.isFavorite(id)
    }

    fun getFavoriteItems(): List<String> {
        return dao.getFavoriteItems()
    }

    suspend fun saveFavoriteItems(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            val result =
                firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(email)
                    .collection(Constants.USER_FAVORITES_COLLECTION).get().await()
            if (result != null) {
                val favoriteIds = result.toObjects(ItemId::class.java)
                favoriteIds.forEach { item ->
                    dao.addFavoriteItem(item)
                }
                return@withContext true
            } else {
                return@withContext false
            }
        }
    }

    suspend fun removeFavoriteItem(id: String) {
        dao.removeFavoriteItem(id)
        val userEmail = EmailPreferences.getEmail(context!!)
        userEmail?.apply {
            firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(userEmail)
                .collection(Constants.USER_FAVORITES_COLLECTION).document(id).delete()

        }
    }

    fun deleteItem(item: PaperItem) {
        if (deleteItemFiles(item)) {
            dao.deleteItem(item)
        }

    }

    private fun deleteItemFiles(item: PaperItem): Boolean {
        var deletePdfSuccess = true
        val pdfFile = File(context!!.filesDir, item.title)
        val coverFile = File(context.filesDir, item.title + "-cover")
        if (pdfFile.exists()) {
            deletePdfSuccess = pdfFile.delete()
        }
        if (coverFile.exists() && deletePdfSuccess) {
            coverFile.delete()
        }

        return deletePdfSuccess
    }


}