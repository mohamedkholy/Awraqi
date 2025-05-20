package com.dev3mk.awraqi.ui.itemPreview

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.dev3mk.awraqi.Base.BaseFragment
import com.dev3mk.awraqi.R
import com.dev3mk.awraqi.data.model.DownloadState
import com.dev3mk.awraqi.data.model.PaperItem
import com.dev3mk.awraqi.databinding.FragmentItemPreviewBinding
import com.dev3mk.awraqi.ui.homeItems.ItemsAdapter
import com.dev3mk.awraqi.util.ConnectivityObserver
import com.dev3mk.awraqi.util.ConnectivityObserver.ConnectionStatus
import com.dev3mk.awraqi.util.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL


class ItemPreviewFragment :
    BaseFragment<FragmentItemPreviewBinding>(R.layout.fragment_item_preview),
    ItemsAdapter.OnItemClickListener {

    private var receiver: BroadcastReceiver? = null
    private val args: ItemPreviewFragmentArgs by navArgs()
    private val item: PaperItem by lazy {
        args.item
    }
    private val viewModel: ItemPreviewViewModel by viewModel()
    private var connectionStatus: ConnectionStatus = ConnectionStatus.Unavailable
    private lateinit var connectionStatusSnackBar: Snackbar

    override fun setup() {
        connectionStatusSnackBar = Snackbar.make(
            requireContext(),
            binding.root,
            getString(R.string.no_internet_connection),
            Snackbar.LENGTH_INDEFINITE
        )

        setFavorite()
        setConnectionStatus()
        setItemData()
        viewModel.getItemDownloadState(item.id).observe(viewLifecycleOwner) { downloadState ->
            item.downloadState = when (downloadState) {
                DownloadState.downloaded -> {
                    binding.buyButton.visibility = if (item.isPurchased) {
                        View.GONE
                    } else View.VISIBLE
                    binding.downloadButton.apply {
                        text = context.getString(R.string.open)
                    }
                    DownloadState.downloaded
                }

                DownloadState.downloading -> {
                    binding.buyButton.visibility = View.GONE
                    binding.downloadButton.text =
                        requireContext().getString(R.string.cancel_download, "")
                    registerDownloadProgressReceiver()
                    DownloadState.downloading
                }

                else -> {
                    binding.buyButton.visibility = if (item.isPurchased) {
                        View.GONE
                    } else View.VISIBLE
                    binding.downloadButton.apply {
                        text =
                            if (item.isPurchased) context.getString(R.string.download) else context.getString(
                                R.string.download_preview
                            )
                    }
                    DownloadState.notDownloded
                }
            }
        }
        binding.buyButton.visibility = if (item.isPurchased) {
            View.GONE
        } else View.VISIBLE

    }

    private fun setFavorite() {
        if (viewModel.isFavorite(item.id))
            binding.favouritesButton.isChecked = true
    }


    private fun registerDownloadProgressReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, p1: Intent?) {
                val bundle = p1?.extras
                val progress = bundle?.getInt(Constants.DOWNLOAD_PROGRESS_KEY)
                if (progress != null && context != null && item.downloadState == DownloadState.downloading) {
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


    @Suppress("DEPRECATION")
    private fun setItemData() {
        val circularProgressDrawable = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        Glide.with(requireContext()).load(URL(item.coverUrl)).placeholder(circularProgressDrawable)
            .into(binding.itemCoverImageView)

        binding.itemTitle.text=item.title
        binding.authorTitle.append(item.author)
        binding.faculty.append(item.faculty)
        binding.subject.append(item.subject)
        binding.university.append(item.university)
        binding.pagesCount.append(getString(R.string.pages, item.pages))
        binding.price.text = getString(R.string.price, item.price)
    }

    override fun addCallbacks() {


        binding.favouritesButton.apply {
            lifecycleScope.launch {
                post {
                    setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                        if (isChecked) {
                            viewModel.addFavoriteItem(item.id)
                        } else {
                            viewModel.removeFavoriteItem(item.id)
                        }
                    }
                }
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buyButton.setOnClickListener {
            if (Firebase.auth.currentUser == null) {
                val action =
                    ItemPreviewFragmentDirections.actionStoreItemsDetailsFragmentToLoginSignupFragment()
                findNavController().navigate(action)
            } else {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.buyFormUrl))
                    requireContext().startActivity(browserIntent)
                }
                 catch (e:ActivityNotFoundException) {
                    Toast.makeText(context,
                        getString(R.string.no_browser_app_found), Toast.LENGTH_SHORT).show()
                }
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
            } catch (e: IllegalArgumentException) {
                Log.e("error", e.message.toString())
            }
        }
        super.onDestroy()
    }

    override fun onClick(paperItem: PaperItem) {

    }

}