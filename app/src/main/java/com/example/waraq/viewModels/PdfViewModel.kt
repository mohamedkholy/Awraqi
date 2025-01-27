package com.example.waraq.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waraq.data.DrawingItem
import com.example.waraq.data.Notes
import com.example.waraq.data.PageNotes
import com.example.waraq.repository.MyRepository
import kotlinx.coroutines.launch

class PdfViewModel:ViewModel(){
    private val myRepository = MyRepository()


    suspend fun getDrawingItem(id:String): DrawingItem? {
        return myRepository.getDrawingItem(id)
    }

    fun upsertDrawingItem(drawingItem: DrawingItem){
        myRepository.upsertDrawingItem(drawingItem)
    }

     fun getAllNotes(pdfId:String): LiveData<Notes> {
        return myRepository.getAllNotes(pdfId)
    }

    fun saveNote(pageNote: PageNotes){
       viewModelScope.launch {
           myRepository.saveNote(pageNote)
       }
    }

    override fun onCleared() {
        super.onCleared()
    }


}