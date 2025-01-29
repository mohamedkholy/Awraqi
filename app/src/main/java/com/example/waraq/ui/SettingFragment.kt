package com.example.waraq.ui

import android.content.Context
import androidx.navigation.fragment.findNavController
import com.example.waraq.R
import com.example.waraq.databinding.FragmentSettingBinding
import com.example.waraq.util.Constants


class SettingFragment:BaseFragment<FragmentSettingBinding>(R.layout.fragment_setting){


    override fun setup() {
        val sp =requireActivity().getSharedPreferences(Constants.DEFAULT_SHARED_PREFERENCES,Context.MODE_PRIVATE)
        val email = sp.getString(Constants.EMAIL_KEY,null)

        if (email!=null) {
            binding.signIn.text = email
            binding.signIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_account_circle_24,0,0,0)
        }
        else { binding.signIn.setOnClickListener { findNavController().navigate(R.id.loginSignupFragment) } }

    }

    override fun addCallbacks() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.preferences.setOnClickListener {
         findNavController().navigate(R.id.preferencesFragment)
        }

    }


}