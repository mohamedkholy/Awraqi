package com.dev3mk.awraqi.ui

import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.databinding.FragmentPolicyBinding


class PolicyFragment : BaseFragment<FragmentPolicyBinding>(R.layout.fragment_policy) {
    override fun setup() {

    }

    override fun addCallbacks() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }


}