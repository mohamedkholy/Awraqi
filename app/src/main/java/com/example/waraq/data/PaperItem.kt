package com.example.waraq.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class PaperItem(
    @PrimaryKey
    val id: String,
    val title: String = "",
    val url: String = "",
    val coverUrl: String = "",
    var downloadState: DownloadState = DownloadState.notDownloded,
    val university: String = "",
    val faculty: String = "",
    val grade: String = "",
    val semester: String = "",
    val subject: String = "",
    val author: String = "",
    val pages: String = "",
    var isPurchased: Boolean = false,
    val price: String = "0",
    ) : Serializable {
    constructor() : this(id = "", isPurchased = false)
}