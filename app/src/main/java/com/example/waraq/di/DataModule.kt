package com.example.waraq.di

import com.example.waraq.data.MyRepository
import com.example.waraq.data.dataSources.LocalDataSource
import com.example.waraq.data.dataSources.RemoteDataSource
import com.example.waraq.util.FirebaseDownload
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        LocalDataSource(androidContext(), get())
    }

    single {
        RemoteDataSource(androidContext())
    }

    single {
        MyRepository(get(), get())
    }

    single {
        FirebaseDownload(androidContext(), get())
    }
}