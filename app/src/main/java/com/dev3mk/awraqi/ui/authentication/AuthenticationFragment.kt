package com.dev3mk.awraqi.ui.authentication

import android.util.Patterns
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.util.Constants
import com.dev3mk.awraqi.data.preferences.EmailPreferences
import com.dev3mk.awraqi.databinding.FragmentAuthenticationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern


class AuthenticationFragment :
    BaseFragment<FragmentAuthenticationBinding>(R.layout.fragment_authentication) {

    private val viewModel by viewModel<AuthenticationViewModel>()

    companion object {
        private const val PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$"
    }

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    override fun setup() {
        binding.isLogging = false
        binding.isSigning = false
    }

    override fun addCallbacks() {

        binding.forgetPassword.setOnClickListener {
            findNavController().navigate(R.id.forgetPasswordFragment)
        }

        binding.backButton.setOnClickListener {
            if (!binding.isSigning!! && !binding.isLogging!!) {
                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!binding.isSigning!! && !binding.isLogging!!) {
                findNavController().popBackStack()
            }
        }

        binding.apply {

            signupButton.setOnClickListener {
                if (!binding.isSigning!! && !binding.isLogging!!) {
                    signUp()
                }

            }

            loginButton.setOnClickListener {
                if (!binding.isSigning!! && !binding.isLogging!!) {
                    lifecycleScope.launch {
                        login()
                    }
                }
            }

            userTypeSelectorRadioGroup.setOnCheckedChangeListener { _, selectedItem ->
                if (selectedItem == userRadio.id) {
                    signupButton.isEnabled = true
                    signupButton.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.primary_color)

                } else {
                    signupButton.isEnabled = false
                    signupButton.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.gray)

                }
            }

            passwordEt.doOnTextChanged { _, _, _, _ ->
                binding.passwordTextInputLayout.isErrorEnabled = false
            }

            emailEt.doOnTextChanged { _, _, _, _ ->
                binding.emailTextInputLayout.isErrorEnabled = false
            }

        }
    }

    private fun signUp() {
        val password = binding.passwordEt.text.toString()
        val email = binding.emailEt.text.toString()
        if (validateEmailAndPassword(email, password)) {
            binding.isSigning = true
            lifecycleScope.launch {
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()

                    onSignupSuccess(email)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.isSigning = false
                    Snackbar.make(binding.root, "Signup failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun login() {
        val password = binding.passwordEt.text.toString()
        val email = binding.emailEt.text.toString()

        if (validateEmailAndPassword(email, password)) {
            binding.isLogging = true
            try {
                val admin =
                    firestore.collection(Constants.FIRE_STORE_ADMIN_COLLECTION).document(email)
                        .get().await()
                if (admin.exists()) {
                    binding.isLogging = false
                    findNavController().navigate(R.id.adminHomeFragment)
                } else {
                    firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    binding.isLogging = false
                    onLoginSuccess(email)
                }
            } catch (e: Exception) {
                binding.isLogging = false
                Snackbar.make(binding.root, "Login failed: ${e.message}", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    private suspend fun onLoginSuccess(email: String) {
        try {
            val student = firestore.collection(Constants.FIRE_STORE_USERS_COLLECTION)
                .document(email)
                .get()
                .await()
            if (student.exists()) {
                viewModel.saveFavoriteItems(email)
                setUserTypeAndNavigateScreen(email)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        val validEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val validPassword = isValidPassword(password)

        if (!validEmail)
            binding.emailTextInputLayout.error = "Email is not valid"

        if (!validPassword)
            binding.passwordTextInputLayout.error =
                "Password must contain mix of upper and lower case letters as well as digits and one special character(8-20)"

        return validEmail && validPassword
    }


    private suspend fun onSignupSuccess(email: String) {
        val map = mapOf("email" to email)
        try {
            firestore.collection(Constants.FIRE_STORE_USERS_COLLECTION)
                .document(email)
                .set(map)
                .await()
            setUserTypeAndNavigateScreen(email)
        } catch (e: Exception) {
            e.printStackTrace()
            binding.isSigning = false
        }
    }


    private fun isValidPassword(password: String): Boolean {
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(password)
        return matcher.matches()
    }

    private suspend fun setUserTypeAndNavigateScreen(email: String) {
        EmailPreferences.saveEmail(requireContext(), email)
        binding.isLogging = false
        binding.isSigning = false
        findNavController().popBackStack()
    }

}