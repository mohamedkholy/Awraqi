package com.dev3mk.awraqi.di

import com.dev3mk.awraqi.ui.homeItems.ItemsViewModel
import com.dev3mk.awraqi.ui.itemPreview.ItemPreviewViewModel
import com.dev3mk.awraqi.ui.authentication.AuthenticationViewModel
import com.dev3mk.awraqi.ui.pdf.PdfViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.dev3mk.awraqi.ui.splash.SplashViewModel

val viewModelModule = module {
    viewModelOf(::PdfViewModel)

    viewModelOf(::ItemsViewModel)

    viewModelOf(::ItemPreviewViewModel)

    viewModelOf(::AuthenticationViewModel)

    viewModelOf(::SplashViewModel)
}