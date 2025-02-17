package com.dev3mk.awraqi.ui.authentication

import androidx.lifecycle.ViewModel
import com.dev3mk.awraqi.data.MyRepository

class AuthenticationViewModel(private val myRepository: MyRepository) : ViewModel() {

    suspend fun saveFavoriteItems(email: String) {
        myRepository.saveFavoriteItems(email)
    }

}