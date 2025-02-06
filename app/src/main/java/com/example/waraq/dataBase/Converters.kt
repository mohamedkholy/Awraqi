package com.example.waraq.dataBase

import androidx.room.TypeConverter
import com.example.waraq.data.Notes
import com.example.waraq.data.PathData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Converters {

    @TypeConverter
    fun fromDrawingItemToString(hashMap: HashMap<Int, MutableList<PathData>>): String {
        return Gson().toJson(hashMap)
    }

    @TypeConverter
    fun fromStringToDrawingItem(string: String): HashMap<Int, MutableList<PathData>> {
        val type = object : TypeToken<HashMap<Int, MutableList<PathData>>>() {}.type
        return Gson().fromJson(string, type)
    }

    @TypeConverter
    fun fromStringToPathData(string: String): PathData {
        val type = object : TypeToken<PathData>() {}.type
        return Gson().fromJson(string, type)
    }

    @TypeConverter
    fun fromPathDataToString(pathData: PathData): String {
        return Gson().toJson(pathData)
    }

    @TypeConverter
    fun fromNoteToString(notes: Notes): String {
        return Gson().toJson(notes)
    }

    @TypeConverter
    fun fromNoteToString(string: String): Notes {
        val type = object : TypeToken<Notes>() {}.type
        return Gson().fromJson(string, type)
    }

}