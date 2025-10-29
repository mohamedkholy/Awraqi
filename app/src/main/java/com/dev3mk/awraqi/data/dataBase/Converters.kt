package com.dev3mk.awraqi.data.dataBase

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.dev3mk.awraqi.data.model.Notes
import com.dev3mk.awraqi.data.model.PathData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@ProvidedTypeConverter
class Converters {

    @TypeConverter
    fun fromDrawingItemToString(hashMap: HashMap<Int, MutableList<PathData>>?): String {
        return Gson().toJson(hashMap ?: HashMap<Int, MutableList<PathData>>())
    }

    @TypeConverter
    fun fromStringToDrawingItem(string: String?): HashMap<Int, MutableList<PathData>> {
        if (string.isNullOrEmpty()) return HashMap()
        return try {
            val type = object : TypeToken<HashMap<Int, MutableList<PathData>>>() {}.type
            Gson().fromJson(string, type) ?: HashMap()
        } catch (e: Exception) {
            HashMap()
        }
    }

    @TypeConverter
    fun fromStringToPathData(string: String?): PathData? {
        if (string.isNullOrEmpty()) return null
        return try {
            val type = object : TypeToken<PathData>() {}.type
            Gson().fromJson(string, type)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromPathDataToString(pathData: PathData?): String {
        return Gson().toJson(pathData)
    }

    @TypeConverter
    fun fromNoteToString(notes: Notes?): String {
        return Gson().toJson(notes)
    }

    @TypeConverter
    fun fromStringToNote(string: String?): Notes? {
        if (string.isNullOrEmpty()) return null
        return try {
            val type = object : TypeToken<Notes>() {}.type
            Gson().fromJson(string, type)
        } catch (e: Exception) {
            null
        }
    }
}