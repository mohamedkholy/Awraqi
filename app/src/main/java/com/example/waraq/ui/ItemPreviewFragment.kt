package com.example.waraq.ui

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.waraq.R
import com.example.waraq.adapters.ItemsAdapter
import com.example.waraq.databinding.FragmentStoreItemsDetailsBinding
import com.example.waraq.data.Downloaded
import com.example.waraq.data.PaperItem
import com.example.waraq.data.Purchased
import com.example.waraq.viewModels.ItemPreviewViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.net.URL


class ItemPreviewFragment :
    BaseFragment<FragmentStoreItemsDetailsBinding>(R.layout.fragment_store_items_details),
    ItemsAdapter.OnItemActionListener {

    private val args: ItemPreviewFragmentArgs by navArgs()
    private val item: PaperItem by lazy {
        args.item
    }
    private val viewModel: ItemPreviewViewModel by viewModels()

    override fun setup() {
        setRecyclerManager()
        setItemData()
        binding.downloadButton.text = if(item.isPurchased==Purchased.PURCHASED) "Download" else "Download preview"
        viewModel.getItemDownloadState(item.id).observe(viewLifecycleOwner) { downloadState ->
            item.downloadState= when (downloadState) {
                    Downloaded.downloaded -> {
                        binding.downloadButton.apply {
                            isEnabled = true
                            text = "Open"
                        }
                       Downloaded.downloaded
                    }

                    Downloaded.downloading -> {
                        binding.downloadButton.apply {
//                            isEnabled = false
                            text = "Cancel download"
                        }
                        Downloaded.downloading
                    }
                    else-> {
                        binding.downloadButton.apply {
                            isEnabled = true
                            text = if(item.isPurchased==Purchased.PURCHASED) "Download" else "Download preview"
                        }
                        Downloaded.notDownloded
                    }
                }
        }
        if (item.isPurchased==Purchased.PURCHASED){
            binding.buyButton.visibility = View.GONE
        }

    }

    private fun setRecyclerManager() {
        val layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.SPACE_AROUND

        }
        binding.recv.layoutManager = layoutManager
    }

    private fun setItemData() {
        Glide.with(requireContext()).load(URL(item.coverUrl)).into(binding.itemCoverImageView)
        binding.authorTitle.text = item.author
        binding.itemTitle.text = item.title
        binding.faculty.text = item.faculty
        binding.subject.text = item.subject
        binding.university.text = item.university
        binding.pagesCount.text = item.pages
    }

    override fun addCallbacks() {


        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.downloadButton.setOnClickListener {
            when (item.downloadState) {
                Downloaded.notDownloded -> {
                    binding.downloadButton.apply {
                        item.downloadState = Downloaded.downloading
                        viewModel.changeItemDownloadState(item)
                    }
                    viewModel.startDownloading(item)
                }
                Downloaded.downloaded -> {
                    val action = ItemPreviewFragmentDirections.actionStoreItemsDetailsFragmentToPdfFragment(item.title!!,item.id)
                    findNavController().navigate(action)
                }
                else -> {
                    viewModel.stopDownloadTask(item)
                }
            }

        }

    }


    override fun onClick(paperItem: PaperItem) {

    }

}