package com.dev3mk.awraqi.ui.pdf


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev3mk.awraqi.data.model.DrawingItem
import com.dev3mk.awraqi.data.model.Notes
import com.dev3mk.awraqi.data.model.PageNotes
import com.dev3mk.awraqi.data.MyRepository
import kotlinx.coroutines.launch

class PdfViewModel(private val myRepository: MyRepository) : ViewModel() {
    private val _notesLiveData = MutableLiveData<Notes>()
    val notesLiveData: LiveData<Notes> = _notesLiveData


    suspend fun getDrawingItem(id: String): DrawingItem? {
        return myRepository.getDrawingItem(id)
    }

    fun upsertDrawingItem(drawingItem: DrawingItem) {
        myRepository.upsertDrawingItem(drawingItem)
    }

    fun getAllNotes(pdfId: String) {
        _notesLiveData.postValue(myRepository.getAllNotes(pdfId))
    }

    fun saveNote(pageNote: PageNotes) {
        viewModelScope.launch {
            myRepository.saveNote(pageNote)
        }
    }


}