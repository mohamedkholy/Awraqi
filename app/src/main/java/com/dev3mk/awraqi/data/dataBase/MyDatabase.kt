package com.dev3mk.awraqi.data.dataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev3mk.awraqi.data.model.DrawingItem
import com.dev3mk.awraqi.data.model.ItemId
import com.dev3mk.awraqi.data.model.PageNotes
import com.dev3mk.awraqi.data.model.PaperItem

@Database(
    entities = [PaperItem::class, DrawingItem::class, PageNotes::class, ItemId::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract val dao: Dao
}