package com.example.waraq.di

import androidx.room.Room
import com.example.waraq.data.dataBase.MyDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATABASE_NAME = "MyDataBase"

val dataBaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), MyDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries().build()
    }
}