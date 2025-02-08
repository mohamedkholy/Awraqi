package com.example.waraq.ui.homeItems

import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.waraq.Base.BaseFragment
import com.example.waraq.R
import com.example.waraq.data.model.DownloadState
import com.example.waraq.data.model.ItemsFilter
import com.example.waraq.data.model.ListItem
import com.example.waraq.data.model.PaperItem
import com.example.waraq.databinding.FragmentDownloadedItemsBinding
import com.example.waraq.ui.user.UserHomeFragmentDirections
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.launch


class DownloadedItemsFragment :
    BaseFragment<FragmentDownloadedItemsBinding>(R.layout.fragment_downloaded_items),
    ItemsAdapter.OnItemClickListener, ItemsAdapter.OnDeleteClickListener {

    private var searchText = ""
    private val viewModel by activityViewModels<ItemsViewModel>()
    private lateinit var adapter: ItemsAdapter
    private var paperItemList = mutableListOf<PaperItem>()
    private var filter: ItemsFilter = ItemsFilter.PURCHASED
    private lateinit var favoriteItems: List<String>


    override fun setup() {
        setRecyclerManager()
    }


    private suspend fun refreshPurchasedItems() {
        favoriteItems = viewModel.getFavoriteItems()
        val iDsList = viewModel.getUserBooksIds()
        if (iDsList.isNotEmpty()) {
            paperItemList.forEach { item ->
                if (iDsList.contains(item.id) && !item.isPurchased) {
                    item.isPurchased = true
                    viewModel.updateItem(item)
                }
            }
        }
        binding.swipeRefreshLayout.isRefreshing = false
        refreshRecyclerView()
    }

    private fun setRecyclerManager() {
        val layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.SPACE_AROUND

        }
        binding.recv.layoutManager = layoutManager
        adapter = ItemsAdapter(this, this)
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
            binding.noItems.visibility = if (newList.isEmpty()) View.VISIBLE else View.GONE
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
                paperItemList.sortedByDescending { it.isPurchased }
                    .toMutableList()
        }

        val list =
            paperItemList.filter { it.title.contains(searchText, ignoreCase = true) }.groupBy {
                when (filter) {
                    ItemsFilter.PURCHASED -> {
                        if (it.isPurchased) "Purchased" else "Available"
                    }

                    ItemsFilter.UNIVERSITY -> it.university
                    ItemsFilter.FACULTY -> it.faculty
                    ItemsFilter.GRADE -> it.grade
                    ItemsFilter.SUBJECT -> it.subject
                    ItemsFilter.FAVORITES -> {
                        if (favoriteItems.contains(it.id)) "Favorites" else "Non-favorite"
                    }
                }
            }.flatMap { (groupName, items) ->
                listOf(ListItem.Header(groupName)) + items.map { ListItem.Item(it) }
            }

        adapter.submitList(list)
    }

    override fun onClick(paperItem: PaperItem) {
        if (paperItem.downloadState != DownloadState.notDownloded) {
            val action =
                UserHomeFragmentDirections.actionUserHomeFragmentToStoreItemsDetailsFragment(
                    paperItem
                )
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
            val navController = navHostFragment.findNavController()
            navController.navigate(action)

        }
    }

    override fun onDelete(item: PaperItem) {
        viewModel.deleteItem(item)
    }
}