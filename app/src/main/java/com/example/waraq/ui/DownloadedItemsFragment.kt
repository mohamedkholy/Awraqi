package com.example.waraq.ui


import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.waraq.R
import com.example.waraq.adapters.ItemsAdapter
import com.example.waraq.data.Downloaded
import com.example.waraq.data.ItemsFilter
import com.example.waraq.data.ListItem
import com.example.waraq.data.PaperItem
import com.example.waraq.databinding.FragmentDownloadedItemsBinding
import com.example.waraq.viewModels.ItemsViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class DownloadedItemsFragment : BaseFragment<FragmentDownloadedItemsBinding>(R.layout.fragment_downloaded_items),
    ItemsAdapter.OnItemActionListener {

    private val viewModel by activityViewModels<ItemsViewModel>()
    private lateinit var adapter: ItemsAdapter
    private var paperItemList = mutableListOf<PaperItem>()
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
        adapter = ItemsAdapter(this)
        binding.recv.adapter = adapter
    }

    override fun addCallbacks() {
        viewModel.itemsFilter.observe(viewLifecycleOwner){filter->
            if( this@DownloadedItemsFragment.filter!=filter){
                this@DownloadedItemsFragment.filter = filter
                presentOnRecyclerVIew()
            }
        }

        viewModel.downloadedItemsLiveData.observe(viewLifecycleOwner) {newList->
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
    }


    override fun onClick(paperItem: PaperItem) {
        if (paperItem.downloadState != Downloaded.notDownloded) {
            val action = UserHomeFragmentDirections.actionUserHomeFragmentToPdfFragment(
                paperItem.title!!,
                paperItem.id
            )
            val navHostFragment =
                requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
            val navController = navHostFragment.findNavController()
            navController.navigate(action)

        }
    }
}