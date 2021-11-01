package com.ktdefter.defter.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.ktdefter.defter.util.getTitleAndFavicon
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton


// TODO Fetch url title and favicon if they don't exist in database.
@Singleton
class BookmarksRepository @Inject constructor(
    private val bookmarksDao: BookmarkDao,
    private val tagDao: TagDao,
    private val bookmarkTagPairDao: BookmarkTagPairDao,
    @ApplicationContext private val context: Context
) {
    fun getBookmarks(): LiveData<List<Bookmark>> {
        return  bookmarksDao.getBookmarks()
//        return bookmarksDao.getBookmarks().map {
//            it.filter { it.favicon == null && false }
//                .map {
//                    GlobalScope.launch {
//                        val newBookmark =
//                            async { getTitleAndFavicon(context, Uri.parse(it.url)) }.await()
//                        Log.d(
//                            "defter",
//                            "There is no saved favicon for ${newBookmark.url}, Trying to fetch."
//                        )
//                        if (newBookmark.favicon == null) {
//                            Log.d("defter", "Cannot fetch favicon for ${newBookmark.url}")
//                        } else {
//                            bookmarksDao.updateBookmark(newBookmark)
//                            Log.d(
//                                "defter",
//                                "Update bookmark is called with ${newBookmark.url} ${newBookmark.favicon}"
//                            )
//                        }
//                    }
//                }
//            return@map it
//        }
    }

    fun getBookmarksSync(): List<Bookmark> = bookmarksDao.getBookmarksSync()
    fun getBookmark(url: String) = bookmarksDao.getBookmark(url)

    fun updateBookmark(
        bookmark: Bookmark,
        fetchTitle: ShouldFetchTitle = ShouldFetchTitle.IfNeeded
    ) {
        bookmarksDao.updateBookmark(bookmark)
        if (fetchTitle == ShouldFetchTitle.Yes || (bookmark.title == null && fetchTitle == ShouldFetchTitle.IfNeeded)) {
            fetchTitle(bookmark)
        }
    }

    enum class ShouldFetchTitle {
        Yes, No, IfNeeded
    }

    fun insertBookmark(bookmark: Bookmark, fetchTitle: ShouldFetchTitle = ShouldFetchTitle.No) {
        if (getBookmark(bookmark.url) == null) {
            bookmarksDao.insertBookmark(bookmark)
            Log.d("Defter", "Inserting bookmark: $bookmark.url")
        }
        if (fetchTitle == ShouldFetchTitle.Yes){
            fetchTitle(bookmark)
        }
    }

    private fun fetchTitle(bookmark: Bookmark) {
        GlobalScope.launch {
            val bookmark =
                async { getTitleAndFavicon(context, Uri.parse(bookmark.url)) }.await()
            bookmarksDao.updateBookmark(bookmark)
        }

    }

    fun deleteBookmark(url: String) = bookmarksDao.deleteBookmark(url)

    fun insertTag(tag: String) = tagDao.insertTag(Tag(tag))

    fun getTags() = tagDao.getTags()

    fun getTagsSync() = tagDao.getTagsSync()

    fun deleteTag(tagName: String) = tagDao.deleteTag(tagName)

    fun addBookmarkTagPair(url: String, tag: String) {
        bookmarkTagPairDao.addBookmarkTagPair(url, tag)
    }

    fun deleteBookmarkTagPair(url: String, tag: String) =
        bookmarkTagPairDao.deletePair(url, tag)

    fun getTagsOfBookmark(url: String) = bookmarkTagPairDao.getTagsWithBookmark(url)

    fun getTagsOfBookmarkSync(url: String) = bookmarkTagPairDao.getTagsWithBookmarkList(url)

    fun getBookmarksOfTag(tag: String) = bookmarkTagPairDao.getBookmarksWithTag(tag)

    fun getBookmarksOfTagSync(tag: String) = bookmarkTagPairDao.getBookmarksWithTagSync(tag)
//
//    companion object {
//
//        @Volatile
//        private var instance: BookmarksRepository? = null
//
//        fun getInstance(
//            bookmarksDao: BookmarkDao,
//            tagDao: TagDao,
//            bookmarkTagPairDao: BookmarkTagPairDao,
//            context: Context?
//        ): BookmarksRepository {
//            return instance ?: synchronized(this) {
//                instance ?: BookmarksRepository(
//                    bookmarksDao,
//                    tagDao,
//                    bookmarkTagPairDao,
//                    context
//                ).also { instance = it }
//            }
//        }
//    }
}
