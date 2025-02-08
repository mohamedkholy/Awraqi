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

    companion object {
        @Volatile
        private var instance: MyDatabase? = null
        private const val DATABASE_NAME = "MyDataBase"

        fun createInstance(context: Context): MyDatabase {
            return instance ?: synchronized(this) {
                build(context).also { instance = it }
            }
        }

        fun createInstance(): MyDatabase {
            return instance!!
        }

        private fun build(context: Context): MyDatabase {
            return Room.databaseBuilder(context, MyDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries().build()
        }

    }

}