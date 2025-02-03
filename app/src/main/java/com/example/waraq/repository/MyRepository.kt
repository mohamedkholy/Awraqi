package com.example.waraq.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.example.waraq.dataBase.MyDatabase
import com.example.waraq.data.DownloadState
import com.example.waraq.data.DrawingItem
import com.example.waraq.data.Notes
import com.example.waraq.data.PageNotes
import com.example.waraq.data.PaperItem
import com.example.waraq.data.Purchased
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
                        item.isPurchased = Purchased.PURCHASED
                }
            }
            return@withContext paperItemList
        }
    }

     suspend fun getUserBooksIds(): List<String> {
        val userEmail = EmailPreferences.getEmail(context!!)
        val booksIdsList = mutableListOf<String>()
        if (userEmail == null)
            return booksIdsList
        val bookIds =
            firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(userEmail)
                .collection(Constants.FIRE_STORE_USER_BOOKS_COLLECTION).get().await()
        bookIds.documents.forEach { book ->
            booksIdsList.add(book.id)
        }
        return booksIdsList
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
        Firebase.storage.getReferenceFromUrl(item.url!!).activeDownloadTasks.forEach {
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

    fun getAllNotes(pdfId: String): LiveData<Notes> {
        return dao.selectFileNotes(pdfId)
    }

    suspend fun saveNote(pageNote: PageNotes) {
        dao.upsertPageNote(pageNote)
    }


}