package com.example.waraq.ui

import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.databinding.FragmentSplashBinding
import com.example.waraq.data.preferences.UserTypePreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SplashFragment :
    BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun setup() {
        val userType:String?
        runBlocking {
            userType = UserTypePreferences.getUserType(requireContext())
        }
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