package com.ktdefter.defter.data
import android.content.Context
import android.util.Log
import com.ktdefter.defter.util.getTitleAndFavicon
import kotlinx.coroutines.*

// TODO Fetch url title and favicon if they don't exist in database.
class BookmarksRepository private constructor(
    private val bookmarksDao: BookmarkDao,
    private val tagDao: TagDao,
    private val bookmarkTagPairDao: BookmarkTagPairDao,
    private  val context: Context
) {

    fun getBookmarks() = bookmarksDao.getBookmarks()

    fun getBookmark(url: String) = bookmarksDao.getBookmark(url)

    fun insertBookmark(url: String) {
        bookmarksDao.insertBookmark(Bookmark(url))
        Log.d("Defter", "Inserting bookmark: $url")
        GlobalScope.launch {
           val bookmark = async {  getTitleAndFavicon(context, url) }.await()
            bookmarksDao.insertBookmark(bookmark)
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
    fun deleteBookmarkTagPair(url: String, tag: String) = bookmarkTagPairDao.deletePair(url, tag)

    fun getTagsOfBookmark(url: String) = bookmarkTagPairDao.getTagsWithBookmark(url)

    fun getTagsOfBookmarkSync(url: String) = bookmarkTagPairDao.getTagsWithBookmarkList(url)

    fun getBookmarksOfTag(tag: String) = bookmarkTagPairDao.getBookmarksWithTag(tag)

    fun getBookmarksOfTagSync(tag: String) = bookmarkTagPairDao.getBookmarksWithTagSync(tag)

    companion object {

        @Volatile private var instance: BookmarksRepository? = null

        fun getInstance(bookmarksDao: BookmarkDao, tagDao: TagDao, bookmarkTagPairDao: BookmarkTagPairDao, context: Context): BookmarksRepository {
            return instance ?: synchronized(this) {
                instance ?: BookmarksRepository(bookmarksDao, tagDao, bookmarkTagPairDao, context).also { instance = it }
            }
        }
    }
}
