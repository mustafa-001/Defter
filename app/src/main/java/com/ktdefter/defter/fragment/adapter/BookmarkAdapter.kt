package com.ktdefter.defter.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import timber.log.Timber

class BookmarkAdapter(val fragmentManager: FragmentManager, val viewModel: BookmarksViewModel) :
    RecyclerView.Adapter<BookmarkViewHolder>() {
    var bookmarks: List<Bookmark> = emptyList()
        set(value) {
            Timber.d("Bookmarks dataset changed.")
            DiffUtil.calculateDiff(
                BookmarkListDiffCallback(field, value)
            ).let { result ->
                result.dispatchUpdatesTo(this)
            }
            field = value
        }

    // Must return a ViewHolder that holds our bookmark view, just inflate our xml and pass it
    // in a ViewHolder. Called before onBindViewHolder()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        return BookmarkViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.bookmark, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val tags = viewModel.getTagsOfBookmarkSync(bookmarks[position].url)
        Timber.d("onBindViewHolder for: ", bookmarks[position])
        holder.onBindToAdapter(bookmarks[position], tags, bookmarks.filter { it.isSelected }.any())
    }


    override fun getItemCount(): Int {
        return bookmarks.size
    }

}

class BookmarkListDiffCallback(
    private val oldList: List<Bookmark>,
    private val newList: List<Bookmark>
) : DiffUtil.Callback() {

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldList.get(oldItemPosition).title != newList.get(newItemPosition).title) {
            return false
        }

        if (oldList.get(oldItemPosition).favicon != newList.get(newItemPosition).favicon) {
            return false
        }
        if (oldList.get(oldItemPosition).isSelected != newList.get(newItemPosition).isSelected) {
            return false
        }

        return true
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList.get(oldItemPosition).url == newList.get(newItemPosition).url
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

}
