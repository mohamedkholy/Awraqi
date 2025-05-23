package com.dev3mk.awraqi.ui


import android.Manifest
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.data.model.ItemsFilter
import com.dev3mk.awraqi.databinding.FragmentUserHomeBinding
import com.dev3mk.awraqi.data.preferences.LanguagePreference
import com.dev3mk.awraqi.ui.homeItems.ItemsViewModel
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale


class UserHomeFragment : BaseFragment<FragmentUserHomeBinding>(R.layout.fragment_user_home) {

    private val viewModel by activityViewModel<ItemsViewModel>()
    private lateinit var searchView: SearchView
    private val translations: HashMap<String, String> = hashMapOf(
        "تم شراؤه" to "Purchased",
        "المفضل" to "Favorites",
        "الجامعة" to "University",
        "الكلية" to "Faculty",
        "الصف" to "Grade",
        "المادة" to "Subject"
    )

    override fun setup() {
        setSpinnerAdapter()
        val navController =
            (childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).findNavController()
        binding.bottomNavigation.setupWithNavController(navController)
        binding.toolbar.inflateMenu(R.menu.user_home_menu)
        prepareSearchView()
        requestNotificationPermission()
    }

    private fun prepareSearchView() {
        searchView = binding.toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
    }

    private fun setSpinnerAdapter() {
        val stringArray = resources.getStringArray(R.array.filter_spinner)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, stringArray)
        adapter.setDropDownViewResource(R.layout.drop_down_spinner_item)
        binding.spinner.adapter = adapter
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }

    override fun addCallbacks() {
        binding.spinner.apply {
            post {
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        view?.apply {
                            var filter = (this as TextView).text.toString()
                            runBlocking {
                                if (Locale.getDefault().language == "ar") {
                                    filter = translations[filter]!!
                                }
                            }
                            viewModel.itemsFilter.postValue(
                                ItemsFilter.valueOf(filter.uppercase())
                            )
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.settings) {
                findNavController().navigate(R.id.settingFragment)
            }
            true
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.searchText.postValue(p0)
                return true
            }
        })
    }

}