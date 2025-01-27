package com.example.waraq.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PageNotes(
    @PrimaryKey
    val pdfId: String,
    val notes:Notes
)