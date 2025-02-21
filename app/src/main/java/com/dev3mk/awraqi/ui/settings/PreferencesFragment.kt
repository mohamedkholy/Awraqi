package com.dev3mk.awraqi.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.MainActivity
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.data.model.Languages
import com.dev3mk.awraqi.data.preferences.ThemePreference
import com.dev3mk.awraqi.data.preferences.LanguagePreference
import com.dev3mk.awraqi.databinding.FragmentPreferencesBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale


class PreferencesFragment :
    BaseFragment<FragmentPreferencesBinding>(R.layout.fragment_preferences) {


    override fun setup() {
        runBlocking {
            binding.langSpinner.apply {
                val stringArray = resources.getStringArray(R.array.langs)
                val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, stringArray)
                adapter.setDropDownViewResource(R.layout.drop_down_spinner_item)
                binding.langSpinner.adapter = adapter
                var lang = LanguagePreference.getLanguage(requireContext())
                if (lang == null) {
                    lang = Locale.getDefault().language
                }
                binding.langSpinner.setSelection(if (lang == "en") 0 else 1)
            }

        }
    }

    override fun addCallbacks() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.nightModeSwitch.apply {
            val nightModeFlags =
                requireContext().resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
                isChecked = true
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
                        p3: Long,
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
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        requireContext().startActivity(intent)
    }


}