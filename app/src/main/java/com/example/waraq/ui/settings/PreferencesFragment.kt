package com.example.waraq.ui.settings

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.MainActivity
import com.example.waraq.R
import com.example.waraq.data.model.Languages
import com.example.waraq.data.model.ThemePreference
import com.example.waraq.databinding.FragmentPreferencesBinding
import com.example.waraq.data.preferences.LanguagePreference
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale


class PreferencesFragment :
    BaseFragment<FragmentPreferencesBinding>(R.layout.fragment_preferences) {


    override fun setup() {
        runBlocking {
            var lang = LanguagePreference.getLanguage(requireContext())
            if (lang==null){
                 lang =Locale.getDefault().language
            }

                binding.langSpinner.setSelection(if (lang == "en") 0 else 1)

        }
    }

    override fun addCallbacks() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.nightModeSwitch.apply {
            runBlocking {
                if (ThemePreference.isNightModeEnabled(requireContext()))
                    isChecked = true
            }
            post {
                setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                    lifecycleScope.launch {
                        ThemePreference.setNightMode(requireContext(), b)
                        restartApp()
                    }

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
                        lifecycleScope.launch {
                            LanguagePreference.saveLanguage(
                                requireContext(),
                                Languages.valueOf(lang).code
                            )
                            restartApp()
                        }

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }
            }
        }
    }


    fun restartApp() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
        requireContext().startActivity(intent)
    }


}