package com.example.waraq.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.databinding.FragmentSettingBinding
import com.example.waraq.util.Constants
import com.example.waraq.data.preferences.EmailPreferences
import kotlinx.coroutines.runBlocking


class SettingFragment : BaseFragment<FragmentSettingBinding>(R.layout.fragment_setting) {


    override fun setup() {
        val email: String?
        runBlocking {
            email = EmailPreferences.getEmail(requireContext())
        }



        if (email != null) {
            binding.signIn.text = email
            binding.signIn.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_account_circle_24,
                0,
                0,
                0
            )
        } else {
            binding.signIn.setOnClickListener { findNavController().navigate(R.id.loginSignupFragment) }
        }

    }

    override fun addCallbacks() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.preferences.setOnClickListener {
            findNavController().navigate(R.id.preferencesFragment)
        }

        binding.helpSupport.setOnClickListener {
            sendSupportEmail()
        }

    }

    private fun sendSupportEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.SUPPORT_EMAIL))
        }

        if (emailIntent.resolveActivity(requireContext().packageManager) != null) {
            requireContext().startActivity(emailIntent)
        } else {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


}