package com.dev3mk.awraqi.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.databinding.FragmentUpdateBinding
import com.dev3mk.awraqi.util.Constants
import kotlin.system.exitProcess


class UpdateFragment : BaseFragment<FragmentUpdateBinding>(R.layout.fragment_update) {

    private val args:UpdateFragmentArgs by navArgs()

    override fun setup() {
        println(args.isMandatory)
        if (args.isMandatory)
        {   binding.action.text= getString(R.string.no_thanks_close_the_app)
            binding.updateMessage.text = getString(R.string.version__no_longer_supported)
        }

    }

    override fun addCallbacks() {
        binding.action.setOnClickListener {
            if (args.isMandatory){
            exitProcess(0)}
            else
            {
                findNavController().navigate(R.id.userHomeFragment)
            }
        }

        binding.updateButton.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.UPDATE_URL))
                requireContext().startActivity(browserIntent)
            }
            catch (e: ActivityNotFoundException) {
                Toast.makeText(context,
                    getString(R.string.no_browser_app_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

}