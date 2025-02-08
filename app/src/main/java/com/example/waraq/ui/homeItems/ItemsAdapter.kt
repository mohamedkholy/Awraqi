package com.example.waraq.ui.homeItems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.waraq.R
import com.example.waraq.data.model.DownloadState
import com.example.waraq.data.model.ListItem
import com.example.waraq.data.model.PaperItem
import java.io.File
import java.net.URL


class ItemsAdapter(
    val ClickListener: OnItemClickListener,
    val deleteListener: OnDeleteClickListener? = null,
) :
    RecyclerView.Adapter<ViewHolder>() {

    companion object {

        const val HEADER_TYPE = 0
        const val DATA_TYPE = 1

        private val diffCallback = object :
            DiffUtil.ItemCallback<ListItem>() {

            override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
                return newItem == oldItem
            }

            override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
                return newItem == oldItem
            }
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            DATA_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.paper_list_item, parent, false)
                MyItemHolder(view)
            }

            HEADER_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_item, parent, false)
                MyHeaderHolder(view)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is MyItemHolder -> {
                val item = (differ.currentList[holder.adapterPosition] as ListItem.Item).data

                if (item.downloadState == DownloadState.downloaded) {
                    Glide.with(holder.view.context)
                        .load(File(holder.view.context.filesDir, item.title + "-cover"))
                        .into(holder.image)
                   deleteListener?.let {
                       holder.options.apply {
                           visibility = View.VISIBLE
                           setOnClickListener {
                               val menu = PopupMenu(holder.view.context, this@apply)
                               menu.inflate(R.menu.delete_item_menu)
                               menu.setOnMenuItemClickListener {
                                   deleteListener.onDelete(item)
                                   true
                               }
                               menu.show()
                           }
                       }
                   }
                } else {
                    val circularProgressDrawable =
                        CircularProgressDrawable(holder.view.context).apply {
                            strokeWidth = 5f
                            centerRadius = 30f
                            start()
                        }
                    Glide.with(holder.view.context).load(URL(item.coverUrl.toString()))
                        .placeholder(circularProgressDrawable).into(holder.image)
                }

                holder.textView.text = item.title
                holder.view.setOnClickListener {
                    ClickListener.onClick(item)
                }
            }

            is MyHeaderHolder -> {
                val item = (differ.currentList[holder.adapterPosition] as ListItem.Header).title
                holder.textView.text = item.lowercase().replaceFirstChar(Char::uppercaseChar)
            }

            else -> {}
        }
    }

    class MyItemHolder(val view: View) : ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.item_cover)
        val textView: TextView = view.findViewById(R.id.item_title)
        val options: ImageView = view.findViewById(R.id.options)
    }

    class MyHeaderHolder(val view: View) : ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.header)
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is ListItem.Header -> HEADER_TYPE
            is ListItem.Item -> DATA_TYPE
        }
    }

    fun submitList(newList: List<ListItem>) {
        differ.submitList(newList)
    }


    interface OnItemClickListener {
        fun onClick(paperItem: PaperItem)
    }

    interface OnDeleteClickListener {
        fun onDelete(item: PaperItem)
    }

}