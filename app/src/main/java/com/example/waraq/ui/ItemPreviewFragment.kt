package com.example.waraq.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.waraq.R
import com.example.waraq.adapters.ItemsAdapter
import com.example.waraq.data.DownloadState
import com.example.waraq.data.PaperItem
import com.example.waraq.data.Purchased
import com.example.waraq.databinding.FragmentItemPreviewBinding
import com.example.waraq.util.ConnectivityObserver
import com.example.waraq.util.ConnectivityObserver.ConnectionStatus
import com.example.waraq.util.Constants
import com.example.waraq.viewModels.ItemPreviewViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.net.URL


class ItemPreviewFragment :
    BaseFragment<FragmentItemPreviewBinding>(R.layout.fragment_item_preview),
    ItemsAdapter.OnItemActionListener {

    private var receiver: BroadcastReceiver?=null
    private val args: ItemPreviewFragmentArgs by navArgs()
    private val item: PaperItem by lazy {
        args.item
    }
    private val viewModel: ItemPreviewViewModel by viewModels()
    private var connectionStatus: ConnectionStatus = ConnectionStatus.Unavailable
    private lateinit var connectionStatusSnackBar: Snackbar

    override fun setup() {
        connectionStatusSnackBar = Snackbar.make(
            requireContext(),
            binding.recv,
            "No internet connection",
            Snackbar.LENGTH_INDEFINITE
        )


        setConnectionStatus()
        setRecyclerManager()
        setItemData()
        binding.downloadButton.text =
            if (item.isPurchased == Purchased.PURCHASED) "Download" else "Download preview"
        viewModel.getItemDownloadState(item.id).observe(viewLifecycleOwner) { downloadState ->
            item.downloadState = when (downloadState) {
                DownloadState.downloaded -> {
                    binding.downloadButton.apply {
                        isEnabled = true
                        text = context.getString(R.string.open)
                    }
                    DownloadState.downloaded
                }

                DownloadState.downloading -> {
                    binding.downloadButton.text =
                        requireContext().getString(R.string.cancel_download, "")
                    registerDownloadProgressReceiver()
                    DownloadState.downloading
                }

                else -> {
                    binding.downloadButton.apply {
                        isEnabled = true
                        text =
                            if (item.isPurchased == Purchased.PURCHASED) context.getString(R.string.download) else context.getString(
                                R.string.download_preview
                            )
                    }
                    DownloadState.notDownloded
                }
            }
        }
        if (item.isPurchased == Purchased.PURCHASED) {
            binding.buyButton.visibility = View.GONE
        }

    }


    private fun registerDownloadProgressReceiver() {
         receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, p1: Intent?) {
                println("receive")
                val bundle = p1?.extras
                val progress = bundle?.getInt(Constants.DOWNLOAD_PROGRESS_KEY)
                if (progress != null && context != null&&item.downloadState==DownloadState.downloading) {
                    binding.downloadButton.text =
                        context.getString(R.string.cancel_download, "$progress%")
                }
            }
        }
        ContextCompat.registerReceiver(
            requireContext(),
            receiver,
            IntentFilter(item.title),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun showNoConnectionSnackBar(show: Boolean) {
        if (show)
            connectionStatusSnackBar.show()
        else
            connectionStatusSnackBar.dismiss()
    }

    private fun setConnectionStatus() {
        lifecycleScope.launch {
            ConnectivityObserver(requireContext()).connectionObserver.collect { status ->
                connectionStatus = status
            }
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
        val circularProgressDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        Glide.with(requireContext()).load(URL(item.coverUrl)).placeholder(circularProgressDrawable)
            .into(binding.itemCoverImageView)
        binding.authorTitle.text = item.author
        binding.itemTitle.text = item.title
        binding.faculty.text = item.faculty
        binding.subject.text = item.subject
        binding.university.text = item.university
        binding.pagesCount.text = getString(R.string.pages, item.pages)
    }

    override fun addCallbacks() {

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buyButton.setOnClickListener {
            if (Firebase.auth.currentUser == null) {
                val action =
                    ItemPreviewFragmentDirections.actionStoreItemsDetailsFragmentToLoginSignupFragment()
                findNavController().navigate(action)
            } else {

            }
        }

        binding.downloadButton.setOnClickListener {
            when (item.downloadState) {
                DownloadState.notDownloded -> {
                    if (connectionStatus == ConnectionStatus.Unavailable) {
                        showNoConnectionSnackBar(true)
                    } else {
                        showNoConnectionSnackBar(false)
                        binding.downloadButton.apply {
                            item.downloadState = DownloadState.downloading
                            viewModel.changeItemDownloadState(item)
                        }
                        viewModel.startDownloading(item)
                    }
                }

                DownloadState.downloaded -> {
                    val action =
                        ItemPreviewFragmentDirections.actionStoreItemsDetailsFragmentToPdfFragment(
                            item
                        )
                    findNavController().navigate(action)
                }

                else -> {
                    requireContext().unregisterReceiver(receiver)
                    viewModel.stopDownloadTask(item)
                }
            }

        }

    }

    override fun onDestroy() {
        receiver?.let {
            try {
                requireContext().unregisterReceiver(receiver)
            }
            catch (e:IllegalArgumentException){
                Log.e("error",e.message.toString())
            }
        }
        super.onDestroy()
    }

    override fun onClick(paperItem: PaperItem) {

    }

}