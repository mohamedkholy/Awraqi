package com.example.waraq.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FavoriteItemId" )
data class ItemId(@PrimaryKey val id:String){
    constructor():this("")
}