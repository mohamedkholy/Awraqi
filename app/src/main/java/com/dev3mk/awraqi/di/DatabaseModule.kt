package com.dev3mk.awraqi.di

import androidx.room.Room
import com.dev3mk.awraqi.data.dataBase.MyDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATABASE_NAME = "MyDataBase"

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), MyDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries().build()
    }
}