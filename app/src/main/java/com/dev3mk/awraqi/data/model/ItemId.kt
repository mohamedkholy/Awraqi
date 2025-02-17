package com.dev3mk.awraqi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FavoriteItemId" )
data class ItemId(@PrimaryKey val id:String){
    constructor():this("")
}