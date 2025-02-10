package com.example.waraq.data.dataSources

import android.content.Context
import android.content.Intent
import com.example.waraq.data.model.DownloadState
import com.example.waraq.data.model.ItemId
import com.example.waraq.data.model.PaperItem
import com.example.waraq.data.preferences.EmailPreferences
import com.example.waraq.services.DownloadItemService
import com.example.waraq.util.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RemoteDataSource(val context: Context) {

    private val firestoreDatabase = Firebase.firestore
    private val storage = Firebase.storage

    suspend fun getAllStoreItems(): List<PaperItem> {
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
            return paperItemList

    }

    fun downloadItem(item: PaperItem) {
            val intent = Intent(context, DownloadItemService::class.java)
            intent.setAction(DownloadItemService.Actions.START.toString())
            intent.putExtra(Constants.SERVICE_INTENT_NAME, item)
            context.startService(intent)
    }


     suspend fun addToFireStore(id: String) {
        val userEmail = EmailPreferences.getEmail(context)
        userEmail?.apply {
            firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(userEmail)
                .collection(Constants.USER_FAVORITES_COLLECTION).document(id)
                .set(ItemId(id))
        }

    }

    suspend fun getUserBooksIds(): List<String> {
        val userEmail = EmailPreferences.getEmail(context)
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


    suspend fun removeFavoriteItem(id: String) {

        val userEmail = EmailPreferences.getEmail(context)
        userEmail?.apply {
            firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(userEmail)
                .collection(Constants.USER_FAVORITES_COLLECTION).document(id).delete()

        }
    }

    fun cancelDownloadTask(item: PaperItem) {
        storage.getReferenceFromUrl(item.url).activeDownloadTasks.forEach {
            it.cancel()
        }
        item.downloadState = DownloadState.notDownloded
    }


    suspend fun getFavoriteItems(email: String):List<ItemId> {
        return withContext(Dispatchers.IO) {
            println("before")
            val result =
                firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(email)
                    .collection(Constants.USER_FAVORITES_COLLECTION).get().await()
            println(result)
            if (result != null) {
                val favoriteIds = result.toObjects(ItemId::class.java)
                return@withContext favoriteIds
            } else {
                return@withContext emptyList()
            }
        }
    }



}