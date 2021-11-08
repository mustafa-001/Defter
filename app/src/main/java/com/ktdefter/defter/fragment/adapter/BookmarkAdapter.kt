package com.ktdefter.defter.fragment.adapter

import android.app.ProgressDialog.show
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.fragment.EditBookmarkFragment
import com.ktdefter.defter.fragment.SelectTagDialogFragment
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.processor.internal.definecomponent.codegen._dagger_hilt_android_components_ActivityComponent
import java.io.File
import javax.inject.Inject

class BookmarkAdapter(val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<BookmarkAdapter.BmViewHolder>() {
    var bookmarks: List<Bookmark> = emptyList()
        set(value) {
            DiffUtil.calculateDiff(
                BookmarkListDiffCallback(field, value)
            ).let { result ->
                result.dispatchUpdatesTo(this)
            }
            field = value
        }

    lateinit var viewModel: BookmarksViewModel

    class BmViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        lateinit var bookmark: Bookmark
        lateinit var tags: LiveData<Tag>
        val titleTextView: TextView = v.findViewById(R.id.bookmark_title_text)
        val urlTextView: TextView = v.findViewById(R.id.bookmark_url_text)
        val tagsTextView: TextView = v.findViewById(R.id.bookmark_tags_text)
        val faviconImageView: ImageView = v.findViewById(R.id.bookmark_image)
    }

    // Must return a ViewHolder that holds our bookmark view, just inflate our xml and pass it
    // in a ViewHolder. Called before onBindViewHolder()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BmViewHolder {
        return BmViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.fragment_bookmark, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BmViewHolder, position: Int) {
        holder.apply {
            bookmarks.get(position).let {
                this.bookmark = it
                this.urlTextView.text = it.hostname
                this.titleTextView.text = it.title

                val imageFile: File? = bookmark.hostname?.let {
                    File(this.itemView.context.filesDir, it)
                }

                if (imageFile != null) {
                    Log.d(
                        "Defter",
                        "Favicon file for ${bookmark.url} is found at ${bookmark.hostname}"
                    )
                    this.faviconImageView.setImageURI(Uri.fromFile(imageFile))
                } else {
                    this.faviconImageView.setImageResource(R.drawable.ic_broken_image_black_24dp)
                    if (bookmark.hostname != null) {
                        Log.d(
                            "Defter",
                            "Favicon file for ${bookmark.url} should be at  ${bookmark.hostname} but not.\n This should be unreachable!"
                        )
                        throw(Exception("Saved favicon cannot be found!"))
                    }
                }

                viewModel.getTagsOfBookmarkSync(it.url).let { tags ->
                    this.tagsTextView.text = tags
                        .map { tag -> tag.tagName }
                        .fold("") { acc, nxt ->
                            "$acc$nxt  "
                        }
                }
            }
        }

        // Android will call this function when creating context menu.
        // menu: context menu being build
        // v: view for which context menu being build
        // menuinfo: extra info about item for which context menu being shown
        // https://developer.android.com/reference/android/view/View.OnCreateContextMenuListener
        // TODO implement other context bar functions; edit eg.
        holder.itemView.setOnCreateContextMenuListener { menu, v, _ ->
            menu.add("Delete").setOnMenuItemClickListener {
                viewModel.deleteBookmark(holder.bookmark.url)
                true
            }

            // TODO We no longer have access to AppCompatActivity context when using Hİlt.,
            // So this navigation throws exception.
            menu.add("Choose tags").setOnMenuItemClickListener {
                val selectTagDialogFragment = SelectTagDialogFragment()
//                val activity =  v.context as AppCompatActivity
                selectTagDialogFragment.apply {
                    selectedBookmark = holder.bookmark
                    show(
                        this@BookmarkAdapter.fragmentManager,
                        "select_tag_dialog"
                    )
                }
                true
            }
            menu.add("Edit Fragment").setOnMenuItemClickListener {
                this@BookmarkAdapter.fragmentManager
                    .primaryNavigationFragment!!
                    .findNavController().navigate(R.id.editBookmarkFragment,
                        Bundle().apply { putString("url", holder.bookmark.url.toString()) })
                true
            }
        }
        holder.itemView.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = sanitizeURL(holder.bookmark.url)
                it.context.startActivity(this)
            }
        }
    }

    private fun sanitizeURL(url: String): Uri {
        return if (Uri.parse(url).scheme == null) {
            Uri.parse(url).buildUpon().scheme("http").build()
        } else {
            Uri.parse(url)
        }
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
