package com.ktdefter.defter.fragment.adapter

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.fragment.BookmarkListFragment
import com.ktdefter.defter.fragment.SelectTagDialogFragment
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import timber.log.Timber
import java.io.File

class BookmarkAdapter(val fragmentManager: FragmentManager, val viewModel: BookmarksViewModel) :
    RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {
    val bookmarks: MutableList<Bookmark> = mutableListOf()

    fun setBookmarks(value: List<Bookmark>) {
            Timber.d("Bookmarks dataset changed.")
            DiffUtil.calculateDiff(
                BookmarkListDiffCallback(bookmarks, value)
            )
                .dispatchUpdatesTo(this)
            bookmarks.clear()
            bookmarks.addAll(value)
        }

    inner class BookmarkViewHolder(v: View) :
        RecyclerView.ViewHolder(v) {
        private val titleTextView: TextView = v.findViewById(R.id.bookmark_title_text)
        private val urlTextView: TextView = v.findViewById(R.id.bookmark_url_text)
        private val tagsTextView: TextView = v.findViewById(R.id.bookmark_tags_text)
        private val faviconImageView: ImageView = v.findViewById(R.id.bookmark_image)
        private val moreOptionsMenu: Button = v.findViewById(R.id.bookmark_more_menu)

        fun onBindToAdapter(bookmark: Bookmark, tags: List<Tag>, multipleSelectionMode: Boolean) {
            Timber.d("onBindToAdapter: " + bookmark.isSelected + "  " + bookmark.url)
            Timber.d("multipleSelectionMode: $multipleSelectionMode")
            this.urlTextView.text = bookmark.hostname
            this.titleTextView.text = bookmark.title
            val imageFile: File? = bookmark.hostname?.let {
                File(this.itemView.context.filesDir, it)
            }

            if (imageFile != null) {
                Timber.d("Favicon file for " + bookmark.url + " is found at " + bookmark.hostname)
                this.faviconImageView.setImageURI(Uri.fromFile(imageFile))
            } else {
                this.faviconImageView.setImageResource(R.drawable.ic_broken_image_black_24dp)
                Timber.d("Favicon file for " + bookmark.url + " should be at " + bookmark.hostname + " but not." + "\n" + " This should be unreachable!")
                throw(Exception("Saved favicon cannot be found!"))
            }

            tags.let { tags ->
                this.tagsTextView.text = tags
                    .map { tag -> tag.tagName }
                    .fold("") { acc, nxt ->
                        "$acc$nxt  "
                    }
            }


            itemView.setOnLongClickListener {
                itemView.findFragment<BookmarkListFragment>().activateMultipleSelection()
                this@BookmarkAdapter.viewModel.markBookmarkSelected(
                    bookmark
                )
                true
            }

            moreOptionsMenu.setOnClickListener {
                itemView.showContextMenu()
            }

            itemView.setOnCreateContextMenuListener { menu, v, _ ->
                menu.add("Delete").setOnMenuItemClickListener {
                    itemView.findFragment<BookmarkListFragment>().bookmarksViewModel.deleteBookmark(
                        bookmark.url
                    )
                    true
                }
                menu.add("Choose tags").setOnMenuItemClickListener {
                    val selectTagDialogFragment = SelectTagDialogFragment()
                    selectTagDialogFragment.apply {
                        selectedBookmark = bookmark
                        show(
                            itemView.findFragment<BookmarkListFragment>().parentFragmentManager,
                            "select_tag_dialog"
                        )
                    }
                    true
                }
                menu.add("Edit Fragment").setOnMenuItemClickListener {
//                this@BookmarkAdapter.fragme<ntManager
                    itemView.findFragment<Fragment>().parentFragmentManager
                        .primaryNavigationFragment!!
                        .findNavController().navigate(R.id.editBookmarkFragment,
                            Bundle().apply { putString("url", bookmark.url.toString()) })
                    true
                }
            }

            itemView.setOnClickListener {
                if (itemView.findFragment<BookmarkListFragment>().bookmarksViewModel.bookmarksToShow.value?.filter { it.isSelected }?.any() == true) {
                    if (bookmark.isSelected) {
                        this@BookmarkAdapter.viewModel.markBookmarkUnselected(
                            bookmark
                        )
                        if (this@BookmarkAdapter.viewModel.bookmarksToShow.value?.filter { it.isSelected }?.size!! == 1){
                           itemView.findFragment<BookmarkListFragment>().disableMultipleSelection()
                        }
                    } else {
                        this@BookmarkAdapter.viewModel.markBookmarkSelected(
                            bookmark
                        )
                    }
                } else {
                    Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = sanitizeURL(bookmark.url)
                        itemView.context.startActivity(this)
                    }
                }
                true
            }
            if (bookmark.isSelected) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.primaryColor)
                )
            } else {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.bookmark_background)
                )
            }
        }

        private fun sanitizeURL(url: String): Uri {
            return if (Uri.parse(url).scheme == null) {
                Uri.parse(url).buildUpon().scheme("http").build()
            } else {
                Uri.parse(url)
            }
        }
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

    override fun onBindViewHolder(
        holder: BookmarkViewHolder,
        position: Int
    ) {
        val tags = viewModel.getTagsOfBookmarkSync(bookmarks[position].url)
        Timber.d("onBindViewHolder for: ", bookmarks[position].url)
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
            Timber.d("Dedected a bookmark with different title ${oldList.get(oldItemPosition).url}")
            return false
        }
        if (oldList.get(oldItemPosition).favicon != newList.get(newItemPosition).favicon) {
            return false
        }
        if (oldList.get(oldItemPosition).isSelected != newList.get(newItemPosition).isSelected) {
            Timber.d("Dedected a bookmark with different isSelected position ${oldList.get(oldItemPosition).url}")
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
