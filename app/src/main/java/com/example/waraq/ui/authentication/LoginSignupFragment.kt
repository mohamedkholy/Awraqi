package com.example.waraq.ui.authentication

import android.util.Patterns
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.databinding.FragmentLoginSignupBinding
import com.example.waraq.util.Constants
import com.example.waraq.data.preferences.EmailPreferences
import com.example.waraq.data.preferences.UserTypePreferences
import com.example.waraq.ui.homeItems.ItemsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern


class LoginSignupFragment :
    BaseFragment<FragmentLoginSignupBinding>(R.layout.fragment_login_signup) {

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
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onSignupSuccess(email)
                }.addOnFailureListener {
                    binding.isSigning = false
                    Snackbar.make(binding.root, "Signup failed", Snackbar.LENGTH_LONG).show()
                }

        }

    }

    private suspend fun login() {
        val password = binding.passwordEt.text.toString()
        val email = binding.emailEt.text.toString()
        if (validateEmailAndPassword(email, password)) {
            binding.isLogging = true
            val admin =
                firestore.collection(Constants.FIRE_STORE_ADMIN_COLLECTION).document(email).get()
                    .await()
            if (admin.exists()) {
                findNavController().navigate(R.id.adminHomeFragment)
                binding.isLogging = false
            } else {
                println("log")
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        lifecycleScope.launch {
                            onLoginSuccess(email)
                        }
                    }.addOnFailureListener {
                        binding.isLogging = false
                        Snackbar.make(binding.root, "Login failed", Snackbar.LENGTH_LONG).show()
                    }

            }
        }

    }

    private suspend fun onLoginSuccess(email: String) {
        val student =
            firestore.collection(Constants.FIRE_STORE_USERS_COLLECTION).document(email).get()
                .await()
        println(student)
        if (student.exists()) {
            println("exists")
            val viewModel = ViewModelProvider(this)[ItemsViewModel::class]
            viewModel.saveFavoriteItems(email)
            setUserTypeAndNavigateScreen(Constants.USER_TYPE, email)
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

    private fun onSignupSuccess(email: String) {
        val map = mapOf("email" to email)
        firestore.collection(Constants.FIRE_STORE_USERS_COLLECTION)
            .document(email).set(map).addOnSuccessListener {
                runBlocking {
                    setUserTypeAndNavigateScreen(Constants.USER_TYPE, email)
                }
            }.addOnFailureListener {
                binding.isSigning = false
            }

    }


    private fun isValidPassword(password: String): Boolean {


        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(password)

        return matcher.matches()
    }

    private suspend fun setUserTypeAndNavigateScreen(userType: String, email: String) {
        EmailPreferences.saveEmail(requireContext(), email)
        UserTypePreferences.saveUserType(requireContext(), userType)
        println(4)
        binding.isLogging = false
        binding.isSigning = false
        findNavController().popBackStack()
    }

}