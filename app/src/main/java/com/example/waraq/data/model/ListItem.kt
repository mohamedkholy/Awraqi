package com.example.waraq.data.model

sealed class ListItem {
    data class Header(val title: String) : ListItem()
    data class Item(val data: PaperItem) : ListItem()
}