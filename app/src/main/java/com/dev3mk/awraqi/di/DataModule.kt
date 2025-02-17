package com.dev3mk.awraqi.di

import com.dev3mk.awraqi.data.MyRepository
import com.dev3mk.awraqi.data.dataSources.LocalDataSource
import com.dev3mk.awraqi.data.dataSources.RemoteDataSource
import com.dev3mk.awraqi.util.FirebaseDownload
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