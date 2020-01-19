package layout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ktdefter.defter.R
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.data.Bookmark

class BookmarkAdapter() : RecyclerView.Adapter<BookmarkAdapter.BmViewHolder>(){

    lateinit var bookmarks: List<Bookmark>
    lateinit var callbacks: OnBookmarkContextMenuListener

    //TODO Document this
    fun setOnBookmarkContextMenuListener(callbacks: OnBookmarkContextMenuListener){
        this.callbacks = callbacks
    }

    class BmViewHolder(v: View): RecyclerView.ViewHolder(v){
        val titleTextView: TextView = v.findViewById(R.id.bookmark_title_text)
        val urlTextView: TextView = v.findViewById(R.id.bookmark_url_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkAdapter.BmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bookmarklist, parent, false)

        return BmViewHolder(view)
    }


    //TODO Show tags.
    override fun onBindViewHolder(holder: BookmarkAdapter.BmViewHolder, position: Int) {
        holder.titleTextView.text = bookmarks.get(position).title
        holder.urlTextView.text = bookmarks.get(position).url

        //Android will call this function when creating context menu.
        //menu: context menu being build
        //v: view for which context menu being build
        //menuinfo: extra info about item for which context menu being shown
        //https://developer.android.com/reference/android/view/View.OnCreateContextMenuListener
        //TODO implement other context bar functions; edit eg.
        holder.itemView.setOnCreateContextMenuListener { menu, v, menuInfo ->
            menu.add("Delete").setOnMenuItemClickListener {
//                Log.d("OnContextMenu", "Pressed for item at of ${v.bookmark_url_text.text} ")
                callbacks.onBookmarkDeleteClicked(bookmarks.get(position).url)
                true
            }
            menu.add("Choose tags").setOnMenuItemClickListener {
                callbacks.onBookmarkChangeTagsClicked(bookmarks.get(position).url)
                true
            }
        }

        holder.itemView.setOnClickListener {
            callbacks.onBookmarkClicked(bookmarks.get(position).url)
        }
    }

    override fun getItemCount(): Int {
        return bookmarks.size
    }


    interface OnBookmarkContextMenuListener{
        fun onBookmarkDeleteClicked(url: String)
        fun onBookmarkChangeTagsClicked(url: String)
        fun onBookmarkClicked(url: String)
    }
}