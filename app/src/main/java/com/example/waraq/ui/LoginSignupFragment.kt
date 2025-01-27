package com.example.waraq.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.waraq.R
import com.example.waraq.databinding.FragmentLoginSignupBinding
import com.example.waraq.util.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern


class LoginSignupFragment :
    BaseFragment<FragmentLoginSignupBinding>(R.layout.fragment_login_signup) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private lateinit var sp:SharedPreferences

    override fun setup() {
        sp=requireContext().getSharedPreferences(Constants.DEFAULT_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        binding.isLogging=false
        binding.isSigning=false
    }

    override fun addCallbacks() {

        binding.apply {

            signupButton.setOnClickListener {
                signUp()
            }

            loginButton.setOnClickListener {
                login()
            }

            userTypeSelectorRadioGroup.setOnCheckedChangeListener { _, selectedItem ->
                if (selectedItem == studentRadio.id) {
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
            binding.isSigning=true
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onSignupSuccess(userId = it.user!!.uid, email = email)
                }.addOnFailureListener {
                    binding.isSigning=false
                    Snackbar.make(binding.root, "Signup failed", Snackbar.LENGTH_LONG).show()
                }

        }

    }


    private fun login() {
        val password = binding.passwordEt.text.toString()
        val email = binding.emailEt.text.toString()
        if (validateEmailAndPassword(email, password)) {
            binding.isLogging=true
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    lifecycleScope.launch {
                        onLoginSuccess(userId = it.user!!.uid)
                    }
                }.addOnFailureListener {
                    binding.isLogging=false
                    Snackbar.make(binding.root, "Login failed", Snackbar.LENGTH_LONG).show()
                }
        }

    }

    private suspend fun onLoginSuccess(userId: String) {
        val student =
            firestore.collection(Constants.FIRE_STORE_STUDENT_COLLECTION).document(userId).get()
                .await()
        if (student.exists()) {
            setUserTypeAndNavigateScreen(Constants.STUDENT_USER_TYPE,LoginSignupFragmentDirections.actionLoginSignupFragmentToUserHomeFragment())
        } else {
            val admin =
                firestore.collection(Constants.FIRE_STORE_ADMIN_COLLECTION).document(userId).get()
                    .await()
            if (admin.exists()) {
                setUserTypeAndNavigateScreen(Constants.ADMIN_USER_TYPE,LoginSignupFragmentDirections.actionLoginSignupFragmentToAdminHomeFragment())
            }
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

    private fun onSignupSuccess(userId: String, email: String) {
        val map = mapOf("email" to email)
        firestore.collection(Constants.FIRE_STORE_STUDENT_COLLECTION)
            .document(userId).set(map).addOnSuccessListener {
                setUserTypeAndNavigateScreen(Constants.STUDENT_USER_TYPE,LoginSignupFragmentDirections.actionLoginSignupFragmentToUserHomeFragment())
            }.addOnFailureListener {
                binding.isSigning=false
            }

    }


    fun isValidPassword(password: String): Boolean {
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$"

        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(password)

        return matcher.matches()
    }

    private fun setUserTypeAndNavigateScreen(userType:String,action: NavDirections) {
        sp.edit().putString(Constants.USER_TYPE,userType).apply()
        binding.isLogging=false
        binding.isSigning=false
        findNavController().navigate(action)
    }

}