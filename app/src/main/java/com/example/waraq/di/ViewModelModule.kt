package com.example.waraq.di

import com.example.waraq.ui.homeItems.ItemsViewModel
import com.example.waraq.ui.itemPreview.ItemPreviewViewModel
import com.example.waraq.ui.pdf.PdfViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::PdfViewModel)

    viewModelOf(::ItemsViewModel)

    viewModelOf(::ItemPreviewViewModel)
}