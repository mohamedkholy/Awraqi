package com.dev3mk.awraqi.ui.splash

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev3mk.awraqi.data.model.Version
import com.dev3mk.awraqi.data.preferences.MandatoryUpdatePreferences
import com.dev3mk.awraqi.util.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashViewModel(private val context: Context) : ViewModel() {


    private var _splashScreenFlag = MutableLiveData(true)
    var splashScreenFlag: LiveData<Boolean> = _splashScreenFlag
    private var _updateStates = MutableLiveData<Version>()
    var updateStates: LiveData<Version> = _updateStates


    suspend fun getLatestVersion() {
        val result = Firebase.firestore.collection(Constants.FIRE_STORE_VERSION)
            .document(Constants.FIRE_STORE_VERSION).get().await()
        val version = result.toObject(Version::class.java)!!
        if (version.mandatory)
        {
            MandatoryUpdatePreferences.saveVersionStatus(context, version)
        }
        _updateStates.postValue(version)
        finishSplash()
    }

    fun finishSplash() {
        viewModelScope.launch {
            delay(1000)
            _splashScreenFlag.postValue(false)
        }
    }


}