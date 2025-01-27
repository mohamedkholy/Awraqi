package com.example.waraq.ui

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.waraq.R
import com.example.waraq.adapters.ItemsAdapter
import com.example.waraq.data.ItemsFilter
import com.example.waraq.data.ListItem
import com.example.waraq.databinding.FragmentStoreItemsBinding
import com.example.waraq.data.PaperItem
import com.example.waraq.viewModels.ItemsViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class StoreItemsFragment : BaseFragment<FragmentStoreItemsBinding>(R.layout.fragment_store_items),
    ItemsAdapter.OnItemActionListener {

    private val viewModel by activityViewModels<ItemsViewModel>()
    private var paperItemList = mutableListOf<PaperItem>()
    private lateinit var adapter: ItemsAdapter
    private var filter: ItemsFilter = ItemsFilter.PURCHASED

    override fun setup() {
       setRecyclerManager()
    }

    private fun setRecyclerManager() {
        val layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.SPACE_AROUND
        }
        binding.recv.layoutManager = layoutManager
        adapter = ItemsAdapter( this)
        binding.recv.adapter = adapter
    }

    override fun addCallbacks() {
        viewModel.itemsFilter.observe(viewLifecycleOwner){filter->
            if( this@StoreItemsFragment.filter!=filter){
                this@StoreItemsFragment.filter = filter
            presentOnRecyclerVIew()
            }
        }

        viewModel.storeItemsLiveData.observe(viewLifecycleOwner) {newList->
            paperItemList = newList.toMutableList()
            presentOnRecyclerVIew()
        }
    }

    private fun presentOnRecyclerVIew() {
        val list = paperItemList.groupBy {
            when (filter) {
                ItemsFilter.PURCHASED -> it.isPurchased.toString()
                ItemsFilter.UNIVERSITY -> it.university
                ItemsFilter.FACULTY -> it.faculty
                ItemsFilter.GRADE -> it.grade
                ItemsFilter.SUBJECT -> it.subject
            }
        }.flatMap {
                (groupName, items) ->
            listOf(ListItem.Header(groupName!!)) + items.map { ListItem.Item(it) }
        }

        adapter.submitList(list)
        binding.progressCircular.visibility=View.GONE
    }

    override fun onClick(paperItem: PaperItem) {
        val action = UserHomeFragmentDirections.actionUserHomeFragmentToStoreItemsDetailsFragment(paperItem)
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.findNavController()
        navController.navigate(action)
    }

}