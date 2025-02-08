package com.example.waraq.ui.admin

import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.databinding.FragmentAdminHomeBinding


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