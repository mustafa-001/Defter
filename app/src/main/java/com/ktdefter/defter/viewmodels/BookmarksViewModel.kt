package com.ktdefter.defter.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.data.Tag

class BookmarksViewModel internal constructor(private val bookmarksRepository: BookmarksRepository) : ViewModel() {
   private var position = 0
    private var lastShownTag: String? = null

    var bookmarksToShow: LiveData<List<Bookmark>> = getBookmarks()
    init {
        bookmarksToShow = if (lastShownTag == null) {
            getBookmarks()
        } else {
            getBookmarksOfTag(lastShownTag as String)
        }
    }

    // TODO Add tags witch swtichmap to bookmarks, dont observer Livedata<List<Tag>>
    // from Adapter
    fun getBookmarks(): LiveData<List<Bookmark>> {
        return Transformations.map(bookmarksRepository.getBookmarks()) {
           it.apply {
               this.map {
                   it.tags = bookmarksRepository.getTagsOfBookmarkSync(it.url)
                       .fold("Tags:\t") { s, t -> s + t.tagName + ", " }
               }
            }
        }
    }

    fun addBookmark(url: String) {
        bookmarksRepository.insertBookmark(url)
   }

    fun deleteBookmark(url: String) {
        bookmarksRepository.deleteBookmark(url)
    }

    fun addBookmarkTagPair(url: String, tag: String) {
        if (!bookmarksRepository
                .getTagsSync()
                .map { it.tagName }
                .contains(tag)) {
            bookmarksRepository.insertTag(tag)
            Log.d("Defter", "adding new $tag")
        }

        Log.d("Defter", "adding tag $tag to $url")
        bookmarksRepository.addBookmarkTagPair(url, tag)
    }

    fun deleteBookmarkTagPair(url: String, tag: String) {
        bookmarksRepository.deleteBookmarkTagPair(url, tag)
        if (getBookmarksOfTagSync(tag).isEmpty()) {
            this.bookmarksRepository.deleteTag(tag)
            Log.d("Defter", "Tag $tag doesn't have any related bookmark, it is deleted")
        }
    }

    fun getTags(): LiveData<List<Tag>> = bookmarksRepository.getTags()

    fun getTagsSync(): List<Tag> = bookmarksRepository.getTagsSync()

    fun getTagsOfBookmark(url: String) = bookmarksRepository.getTagsOfBookmark(url)

    fun getTagsOfBookmarkSync(url: String) = bookmarksRepository.getTagsOfBookmarkSync(url)

    fun getBookmarksOfTag(tag: String?): LiveData<List<Bookmark>> {
        return if (tag == null) {
            getBookmarks()
        } else {
            bookmarksRepository.getBookmarksOfTag(tag)
        }
    }

        fun getBookmarksOfTagSync(tag: String) = bookmarksRepository.getBookmarksOfTagSync(tag)
}
