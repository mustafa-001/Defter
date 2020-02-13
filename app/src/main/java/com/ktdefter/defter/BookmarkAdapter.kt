package layout

import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.R
import com.ktdefter.defter.SelectTagDialogFragment
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception

class BookmarkAdapter() : RecyclerView.Adapter<BookmarkAdapter.BmViewHolder>() {

    var bookmarks: List<Bookmark> = listOf(Bookmark(url = "starting"))
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkAdapter.BmViewHolder {
        return BmViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.bookmark_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookmarkAdapter.BmViewHolder, position: Int) {
        holder.apply {
            bookmarks.get(position).let {
                this.bookmark = it
                this.urlTextView.text = it.getHostname()
                this.titleTextView.text = it.title

                val imageFile: File? = bookmark.favicon?.let{
                    File(this.itemView.context.filesDir, it)
                } ?: run {
                    Log.d("Defter", "Favicon file for ${bookmark.getHostname()} not found")
                    null
                }

                if (imageFile != null) {
                    this.faviconImageView.setImageURI(Uri.fromFile(imageFile))
                } else {
                    this.faviconImageView.setImageResource(R.drawable.ic_broken_image_black_24dp)
                }

                viewModel.getTagsOfBookmark(it.url).observe(holder.itemView.context as AppCompatActivity) { tags ->
                    this.tagsTextView.text = tags
                        .map { tag -> tag.tagName }
                        .fold("") { acc, nxt -> acc  + nxt + "  "
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

            menu.add("Choose tags").setOnMenuItemClickListener {
                val selectTagDialogFragment = SelectTagDialogFragment()
                val activity = v.context as AppCompatActivity
                selectTagDialogFragment.apply {
                    selectedBookmark = holder.bookmark
                    show(
                        activity.supportFragmentManager,
                        "select_tag_dialog"
                    )
                }

                true
            }
        }

        holder.itemView.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(holder.bookmark.url)
                it.context.startActivity(this)
            }
        }
    }

    override fun getItemCount(): Int {
        return bookmarks.size
    }
}
