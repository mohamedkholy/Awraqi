package com.dev3mk.awraqi.data.dataSources

import android.content.Context
import android.content.Intent
import com.dev3mk.awraqi.data.model.DownloadState
import com.dev3mk.awraqi.data.model.ItemId
import com.dev3mk.awraqi.data.model.PaperItem
import com.dev3mk.awraqi.data.preferences.EmailPreferences
import com.dev3mk.awraqi.services.DownloadItemService
import com.dev3mk.awraqi.util.Constants
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
        try {
            val userItemIdList = getUserBooksIds()
            val task = try {
                firestoreDatabase.collection(Constants.FIRE_STORE_BOOKS_COLLECTION)
                    .get().await()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (task != null) {
                paperItemList = task.toObjects(PaperItem::class.java)
            }
            if (userItemIdList.isNotEmpty()) {
                paperItemList.forEach { item ->
                    if (userItemIdList.contains(item.id))
                        item.isPurchased = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            try {
                firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION)
                    .document(userEmail)
                    .collection(Constants.USER_FAVORITES_COLLECTION).document(id)
                    .set(ItemId(id)).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getUserBooksIds(): List<String> {
        val userEmail = EmailPreferences.getEmail(context)
        var booksIdsList = mutableListOf<ItemId>()
        if (userEmail == null)
            return mutableListOf()
        try {
            val bookIds = firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION)
                .document(userEmail)
                .collection(Constants.FIRE_STORE_USER_BOOKS_COLLECTION).get().await()
            if (!bookIds.isEmpty) {
                booksIdsList = bookIds.toObjects(ItemId::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return booksIdsList.map { it.id }
    }


    suspend fun removeFavoriteItem(id: String) {
        val userEmail = EmailPreferences.getEmail(context)
        userEmail?.apply {
            try {
                firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION)
                    .document(userEmail)
                    .collection(Constants.USER_FAVORITES_COLLECTION).document(id).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelDownloadTask(item: PaperItem) {
        storage.getReferenceFromUrl(item.url).activeDownloadTasks.forEach {
            it.cancel()
        }
        item.downloadState = DownloadState.notDownloded
    }


    suspend fun getFavoriteItems(email: String): List<ItemId> {
        return withContext(Dispatchers.IO) {
            try {
                println("before")
                val result = firestoreDatabase.collection(Constants.FIRE_STORE_USERS_COLLECTION)
                    .document(email)
                    .collection(Constants.USER_FAVORITES_COLLECTION).get().await()
                println(result)
                if (result != null) {
                    val favoriteIds = result.toObjects(ItemId::class.java)
                    return@withContext favoriteIds
                } else {
                    return@withContext emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()
            }
        }
    }


}