package com.example.waraq.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.waraq.MainActivity
import com.example.waraq.R
import com.example.waraq.data.Languages
import com.example.waraq.data.ThemePreference
import com.example.waraq.databinding.FragmentPreferencesBinding
import com.example.waraq.util.LanguagePreference
import com.example.waraq.util.LocaleHelper


class PreferencesFragment :
    BaseFragment<FragmentPreferencesBinding>(R.layout.fragment_preferences) {


    override fun setup() {
        var lang = LanguagePreference.getLanguage(requireContext())
        lang?.apply {
            binding.langSpinner.setSelection(if (lang=="en")  1 else 0)
        }
    }

    override fun addCallbacks() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.nightModeSwitch.apply {
            if (ThemePreference.isNightModeEnabled(requireContext()))
                isChecked = true
            post{
                setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->
                    ThemePreference.setNightMode(requireContext(),b)
                    restartApp()
                }
        }
        }
        binding.langSpinner.apply {
            post {
                onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        view: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        val lang = (view as TextView).text.toString().uppercase()
                        LanguagePreference.saveLanguage(requireContext(), Languages.valueOf(lang).code)
                        restartApp()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }
            }
        }
    }



    fun restartApp() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        requireContext().startActivity(intent)
    }


}