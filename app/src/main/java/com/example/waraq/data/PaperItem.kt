package com.example.waraq.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class PaperItem(
    @PrimaryKey
    val id: String,
    val title: String? = null,
    val url: String? = null,
    val coverUrl: String? = null,
    var downloadState: DownloadState? = DownloadState.notDownloded,
    val university:String? = null,
    val faculty:String? = null,
    val grade:String? = null,
    val semester:String? = null,
    val subject:String? = null,
    val author:String? = null,
    val pages:String? = null,
    val previewLink:String? = null,
    var isPurchased:Purchased = Purchased.AVAILABLE

    ):Serializable{
    constructor():this(id="",isPurchased=Purchased.AVAILABLE)
}