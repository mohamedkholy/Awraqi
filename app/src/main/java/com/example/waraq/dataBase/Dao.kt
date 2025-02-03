package com.example.waraq.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.waraq.data.DownloadState
import com.example.waraq.data.DrawingItem
import com.example.waraq.data.Notes
import com.example.waraq.data.PageNotes
import com.example.waraq.data.PaperItem

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
    fun selectFileNotes(pdfId: String):LiveData<Notes>


}