package com.dev3mk.awraqi.ui.splash

import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.data.model.Version
import com.dev3mk.awraqi.data.preferences.MandatoryUpdatePreferences
import com.dev3mk.awraqi.databinding.FragmentSplashBinding
import com.dev3mk.awraqi.util.ConnectivityObserver
import com.dev3mk.awraqi.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class SplashFragment :
    BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    private val viewModel: SplashViewModel by activityViewModel()

    override fun setup() {
        lifecycleScope.launch {
            val connectivityObserver = ConnectivityObserver(requireContext()).connectionObserver
            val status = connectivityObserver.first()
            if (status == ConnectivityObserver.ConnectionStatus.Available) {
                viewModel.getLatestVersion()
                viewModel.updateStates.observe(viewLifecycleOwner) { version ->
                    if (version.version != Constants.CURRENT_VERSION) {
                        navigate(
                            SplashFragmentDirections.actionSplashFragmentToUpdateFragment(
                                checkIfMandatory()
                            )
                        )
                    } else {
                        navigate(SplashFragmentDirections.actionSplashFragmentToUserHomeFragment())
                    }
                }
            } else {
                if (checkIfMandatory())
                {
                    navigate(
                        SplashFragmentDirections.actionSplashFragmentToUpdateFragment(
                            true
                        )
                    )
                }
                else {
                    navigate(SplashFragmentDirections.actionSplashFragmentToUserHomeFragment())

                }
                viewModel.finishSplash()
            }
        }
    }

    private fun checkIfMandatory(): Boolean {
        val lastMandatoryVersion :Version?
        runBlocking {
           lastMandatoryVersion = MandatoryUpdatePreferences.getMandatoryVersion(requireContext())
        }
        if (lastMandatoryVersion==null) return false
        val lastMandatoryVersionInt = lastMandatoryVersion.version.filterNot { it=='.' }
        val currentVersion = Constants.CURRENT_VERSION.filterNot { it=='.' }
        return lastMandatoryVersionInt > currentVersion
    }


    override fun addCallbacks() {

    }

    private fun navigate(action: NavDirections) {
        findNavController().navigate(action)
    }


}