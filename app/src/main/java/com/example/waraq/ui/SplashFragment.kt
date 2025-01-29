package com.example.waraq.ui

import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.waraq.R
import com.example.waraq.databinding.FragmentSplashBinding
import com.example.waraq.util.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SplashFragment :
    BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun setup() {
        val sp = requireContext().getSharedPreferences(
            Constants.DEFAULT_SHARED_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val userType = sp.getString(Constants.USER_TYPE, null)
//        navigate(SplashFragmentDirections.actionSplashFragmentToUserGraph())
//        if (firebaseAuth.currentUser == null) {
//            navigate(SplashFragmentDirections.actionSplashFragmentToLoginSignupFragment())
//        } else if (userType == Constants.ADMIN_USER_TYPE) {
//            navigate(SplashFragmentDirections.actionSplashFragmentToAdminHomeFragment())
//        } else if (userType == Constants.STUDENT_USER_TYPE) {
//            navigate(SplashFragmentDirections.actionSplashFragmentToUserHomeFragment())
//        }


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