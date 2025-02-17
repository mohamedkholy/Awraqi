package com.dev3mk.awraqi.ui.authentication


import android.util.Patterns
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.databinding.FragmentForgetPasswordBinding
import com.dev3mk.awraqi.util.KeyboardUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class ForgetPasswordFragment :
    BaseFragment<FragmentForgetPasswordBinding>(R.layout.fragment_forget_password) {
    override fun setup() {
        binding.sending = false
        binding.emailEt.requestFocus()
    }

    override fun addCallbacks() {

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.emailEt.doOnTextChanged { _, _, _, _ ->
            binding.emailTextInputLayout.isErrorEnabled = false
        }

        binding.sendButton.setOnClickListener {
            val email = binding.emailEt.text.toString()
            binding.emailEt.clearFocus()
            KeyboardUtils.closeKeyboard(requireContext(), binding.emailEt)
            if (!binding.sending!! && validateEmailAndPassword(email)) {
                binding.sending = true
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.reset_email_was_sent_successfully),
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.emailEt.text = null
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.failed), Snackbar.ANIMATION_MODE_FADE
                        ).show()
                    }
                    binding.sending = false
                }

            }
        }
    }

    private fun validateEmailAndPassword(email: String): Boolean {
        val validEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (!validEmail)
            binding.emailTextInputLayout.error = "Email is not valid"
        return validEmail
    }

}