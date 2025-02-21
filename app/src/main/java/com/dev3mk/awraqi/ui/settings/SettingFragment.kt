package com.dev3mk.awraqi.ui.settings

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.databinding.FragmentSettingBinding
import com.dev3mk.awraqi.util.Constants
import com.dev3mk.awraqi.data.preferences.EmailPreferences
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
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
            binding.logout.visibility = View.VISIBLE
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

        binding.policy.setOnClickListener {
            findNavController().navigate(R.id.policyFragment)
        }

        binding.helpSupport.setOnClickListener {
            sendSupportEmail()
        }

        binding.logout.setOnClickListener {
            signOut()
        }

    }

    private fun signOut() {
        Firebase.auth.signOut()
        binding.signIn.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.baseline_account_circle_24,
            0,
            R.drawable.baseline_arrow_forward_ios,
            0
        )
        binding.logout.visibility = View.GONE
        lifecycleScope.launch { EmailPreferences.saveEmail(requireContext(),null) }
        binding.signIn.setOnClickListener { findNavController().navigate(R.id.loginSignupFragment) }
        binding.signIn.text = getString(R.string.sign_in)
    }

    private fun sendSupportEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.SUPPORT_EMAIL))
        }

        if (emailIntent.resolveActivity(requireContext().packageManager) != null) {
            requireContext().startActivity(emailIntent)
        } else {
            Toast.makeText(context, getString(R.string.no_email_app_found), Toast.LENGTH_SHORT).show()
        }
    }


}