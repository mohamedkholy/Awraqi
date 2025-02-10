package com.example.waraq.data.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.waraq.data.model.DrawingItem
import com.example.waraq.data.model.ItemId
import com.example.waraq.data.model.PageNotes
import com.example.waraq.data.model.PaperItem

@Database(
    entities = [PaperItem::class, DrawingItem::class, PageNotes::class, ItemId::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract val dao: Dao
}