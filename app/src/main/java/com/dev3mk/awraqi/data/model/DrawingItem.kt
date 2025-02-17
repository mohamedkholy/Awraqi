package com.dev3mk.awraqi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DrawingItem(
  @PrimaryKey
  val id: String,
  val pathMap: HashMap<Int, MutableList<PathData>>
)


