package com.dev3mk.awraqi.ui

import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.data.model.Version
import com.dev3mk.awraqi.databinding.FragmentSplashBinding
import com.dev3mk.awraqi.data.preferences.UserTypePreferences
import com.dev3mk.awraqi.util.ConnectivityObserver
import com.dev3mk.awraqi.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class SplashFragment :
    BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    override fun setup() {
        lifecycleScope.launch {
            val update = IsOutDated()
            if (update.first) {
                navigate(SplashFragmentDirections.actionSplashFragmentToUpdateFragment(update.second))
            }  else {
                navigate(SplashFragmentDirections.actionSplashFragmentToUserHomeFragment())
            }
        }


    }

    private suspend fun IsOutDated(): Pair<Boolean,Boolean> {
        val appVersion=Constants.CURRENT_VERSION
        val result = Firebase.firestore.collection(Constants.FIRE_STORE_VERSION).document(Constants.FIRE_STORE_VERSION).get().await()
        val currentVersion = result.toObject(Version::class.java)!!
        println(currentVersion.version)
        println(currentVersion.mandatory)
        return Pair(appVersion != currentVersion.version,currentVersion.mandatory)
    }

    override fun addCallbacks() {

    }

    private fun navigate(action: NavDirections) {
        lifecycleScope.launch {
//            delay(2500)
            findNavController().navigate(action)
        }
    }


}