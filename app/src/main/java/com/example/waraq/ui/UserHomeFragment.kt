package com.example.waraq.ui


import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.data.model.ItemsFilter
import com.example.waraq.databinding.FragmentUserHomeBinding
import com.example.waraq.data.preferences.LanguagePreference
import com.example.waraq.ui.homeItems.ItemsViewModel
import kotlinx.coroutines.runBlocking


class UserHomeFragment : BaseFragment<FragmentUserHomeBinding>(R.layout.fragment_user_home) {

    private val viewModel by activityViewModels<ItemsViewModel>()
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
                                if (LanguagePreference.getLanguage(requireContext()) == "ar") {
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