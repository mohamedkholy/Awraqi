package com.example.waraq.ui.homeItems

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.data.model.ItemsFilter
import com.example.waraq.data.model.ListItem
import com.example.waraq.databinding.FragmentStoreItemsBinding
import com.example.waraq.data.model.PaperItem
import com.example.waraq.ui.UserHomeFragmentDirections
import com.example.waraq.util.ConnectivityObserver
import com.example.waraq.util.ConnectivityObserver.ConnectionStatus
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class StoreItemsFragment : BaseFragment<FragmentStoreItemsBinding>(R.layout.fragment_store_items),
    ItemsAdapter.OnItemClickListener {

    private val viewModel by activityViewModel<ItemsViewModel>()
    private var paperItemList = mutableListOf<PaperItem>()
    private lateinit var adapter: ItemsAdapter
    private var filter: ItemsFilter = ItemsFilter.PURCHASED
    private var searchText = ""
    private var connectionStatus: ConnectionStatus = ConnectionStatus.Unavailable
    private lateinit var connectionStatusSnackBar:Snackbar
    private lateinit var favoriteItems:List<String>


    override fun setup() {
        favoriteItems = viewModel.getFavoriteItems()
        binding.swipeRefreshLayout.isRefreshing = true
        connectionStatusSnackBar = Snackbar.make(
            requireContext(),
            binding.recv,
            getString(R.string.no_internet_connection),
            Snackbar.LENGTH_INDEFINITE
        )
        setConnectionStatus()
        setRecyclerManager()
    }

    private fun setRecyclerManager() {
        val layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.SPACE_AROUND
        }
        binding.recv.layoutManager = layoutManager
        adapter = ItemsAdapter(this)
        binding.recv.adapter = adapter
    }

    override fun addCallbacks() {

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (connectionStatus == ConnectionStatus.Unavailable) {
                binding.swipeRefreshLayout.isRefreshing = false
                showNoConnectionSnackBar(true)
            } else
                viewModel.getStoreBooks()
        }

        viewModel.itemsFilter.observe(viewLifecycleOwner) { filter ->
            if (this@StoreItemsFragment.filter != filter) {
                this@StoreItemsFragment.filter = filter
                refreshRecyclerView()
            }
        }

        viewModel.storeItemsLiveData.observe(viewLifecycleOwner) { list ->
            paperItemList = list.toMutableList()
            refreshRecyclerView()
        }


        viewModel.searchText.observe(viewLifecycleOwner) { searchText ->
            if (searchText != this@StoreItemsFragment.searchText) {
                this@StoreItemsFragment.searchText = searchText
                refreshRecyclerView()
            }
        }
    }

    private fun showNoConnectionSnackBar(show:Boolean) {
        if (show)
            connectionStatusSnackBar.show()
        else
            connectionStatusSnackBar.dismiss()
    }

    private fun setConnectionStatus() {
        lifecycleScope.launch {
            ConnectivityObserver(requireContext()).connectionObserver.collect { status ->
                if (status == ConnectionStatus.Unavailable) {
                    showNoConnectionSnackBar(true)
                }
                else{
                    showNoConnectionSnackBar(false)
                }

                connectionStatus = status
            }
        }
    }

    private fun refreshRecyclerView() {
        if (filter == ItemsFilter.PURCHASED) {
            paperItemList =
                paperItemList.sortedByDescending { it.isPurchased }
                    .toMutableList()
        }

        val list =
            paperItemList.filter { it.title.contains(searchText, ignoreCase = true) }.groupBy {
                when (filter) {
                    ItemsFilter.PURCHASED -> {
                        if(it.isPurchased)
                            "Purchased"
                        else
                            "Available"
                    }
                    ItemsFilter.UNIVERSITY -> it.university
                    ItemsFilter.FACULTY -> it.faculty
                    ItemsFilter.GRADE -> it.grade
                    ItemsFilter.SUBJECT -> it.subject
                    ItemsFilter.FAVORITES -> {
                        if (favoriteItems.contains(it.id))
                            "Favorites"
                        else
                            "Non-favorite"
                    }
                }
            }.flatMap { (groupName, items) ->
                listOf(ListItem.Header(groupName)) + items.map { ListItem.Item(it) }
            }

        adapter.submitList(list)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onClick(paperItem: PaperItem) {
        val action =
            UserHomeFragmentDirections.actionUserHomeFragmentToStoreItemsDetailsFragment(paperItem)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.findNavController()
        navController.navigate(action)
    }

}