package com.example.waraq.ui

import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.waraq.R
import com.example.waraq.adapters.ItemsAdapter
import com.example.waraq.data.DownloadState
import com.example.waraq.data.ItemsFilter
import com.example.waraq.data.ListItem
import com.example.waraq.data.PaperItem
import com.example.waraq.data.Purchased
import com.example.waraq.databinding.FragmentDownloadedItemsBinding
import com.example.waraq.viewModels.ItemsViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.launch



class DownloadedItemsFragment :
    BaseFragment<FragmentDownloadedItemsBinding>(R.layout.fragment_downloaded_items),
    ItemsAdapter.OnItemActionListener {

    private var searchText = ""
    private val viewModel by activityViewModels<ItemsViewModel>()
    private lateinit var adapter: ItemsAdapter
    private var paperItemList = mutableListOf<PaperItem>()
    private var filter: ItemsFilter = ItemsFilter.PURCHASED


    override fun setup() {
        setRecyclerManager()
    }


    private suspend fun refreshPurchasedItems() {
        val iDsList=viewModel.getUserBooksIds()
        if (iDsList.isNotEmpty()) {
            paperItemList.forEach { item ->
                if (iDsList.contains(item.id)&&item.isPurchased!=Purchased.PURCHASED){
                    item.isPurchased = Purchased.PURCHASED
                    viewModel.updateItem(item)
                }
            }
        }
        binding.swipeRefreshLayout.isRefreshing=false
        refreshRecyclerView()
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
            lifecycleScope.launch {
                refreshPurchasedItems()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.storeItemsFragment)
        }

        viewModel.itemsFilter.observe(viewLifecycleOwner) { filter ->
            if (this@DownloadedItemsFragment.filter != filter) {
                this@DownloadedItemsFragment.filter = filter
                refreshRecyclerView()
            }
        }

        viewModel.downloadedItemsLiveData.observe(viewLifecycleOwner) { newList ->
            binding.noItemsLayout.visibility = if (newList.isEmpty()) View.VISIBLE else View.GONE
            paperItemList = newList.toMutableList()
            lifecycleScope.launch {
                refreshPurchasedItems()
            }
            refreshRecyclerView()
        }


        viewModel.searchText.observe(viewLifecycleOwner) { searchText ->
            if (searchText != this@DownloadedItemsFragment.searchText) {
                this@DownloadedItemsFragment.searchText = searchText
                refreshRecyclerView()
            }
        }

    }

    private fun refreshRecyclerView() {
        if (filter == ItemsFilter.PURCHASED) {
            paperItemList =
                paperItemList.sortedByDescending { it.isPurchased == Purchased.PURCHASED }
                    .toMutableList()
        }

        val list =
            paperItemList.filter { it.title!!.contains(searchText, ignoreCase = true) }.groupBy {
                when (filter) {
                    ItemsFilter.PURCHASED -> it.isPurchased.toString()
                    ItemsFilter.UNIVERSITY -> it.university
                    ItemsFilter.FACULTY -> it.faculty
                    ItemsFilter.GRADE -> it.grade
                    ItemsFilter.SUBJECT -> it.subject
                }
            }.flatMap { (groupName, items) ->
                listOf(ListItem.Header(groupName!!)) + items.map { ListItem.Item(it) }
            }

        adapter.submitList(list)
    }


    override fun onClick(paperItem: PaperItem) {
        if (paperItem.downloadState != DownloadState.notDownloded) {
            val action = UserHomeFragmentDirections.actionUserHomeFragmentToPdfFragment(
                paperItem
            )
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
            val navController = navHostFragment.findNavController()
            navController.navigate(action)

        }
    }
}