package com.dev3mk.awraqi.ui.admin

import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.databinding.FragmentAdminHomeBinding


class AdminHomeFragment : BaseFragment<FragmentAdminHomeBinding>(R.layout.fragment_admin_home) {


    override fun setup() {

    }

    override fun addCallbacks() {

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

      binding.addItemButton.setOnClickListener {
          findNavController().navigate(R.id.addItemFragment)
      }

        binding.assignItemButton.setOnClickListener {
            findNavController().navigate(R.id.assignItemFragment)
        }
    }

}