package com.dev3mk.awraqi.data.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.dev3mk.awraqi.data.model.DownloadState
import com.dev3mk.awraqi.data.model.DrawingItem
import com.dev3mk.awraqi.data.model.ItemId
import com.dev3mk.awraqi.data.model.Notes
import com.dev3mk.awraqi.data.model.PageNotes
import com.dev3mk.awraqi.data.model.PaperItem

@Dao
interface Dao {
    @Query("select * from PaperItem where downloadState = 'downloaded' ")
    fun getAllItems(): LiveData<List<PaperItem>>

    @Upsert
    suspend fun upsertItem(paperItem: PaperItem)


    @Query("select downloadState from PaperItem where id= :id ")
    fun getItemDownloadState(id: String): LiveData<DownloadState>

    @Upsert
    suspend fun upsertDrawingItem(drawingItem: DrawingItem)

    @Query("select * from drawingitem where id= :id ")
    suspend fun getDrawingItem(id: String): DrawingItem?

    @Upsert
    suspend fun upsertPageNote(pageNote: PageNotes)

    @Query("select notes from pagenotes where pdfId=:pdfId")
    fun selectFileNotes(pdfId: String): Notes

    @Upsert
    fun addFavoriteItem(favoriteItemId: ItemId)

    @Query("SELECT EXISTS(SELECT * FROM favoriteitemid WHERE id = :id)")
    fun isFavorite(id: String):Boolean

    @Query("select id from favoriteitemid")
    fun getFavoriteItems(): List<String>

    @Query("delete from FavoriteItemId where id=:id")
    fun removeFavoriteItem(id: String)

    @Delete
    fun deleteItem(item: PaperItem)

}