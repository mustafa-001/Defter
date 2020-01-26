package com.ktdefter.defter.data

// TODO Fetch url title and favicon if they don't exist in database.
class BookmarksRepository private constructor(
    private val bookmarksDao: BookmarkDao,
    private val tagDao: TagDao,
    private val bookmarkTagPairDao: BookmarkTagPairDao
) {

    fun getBookmarks() = bookmarksDao.getBookmarks()

    fun getBookmark(url: String) = bookmarksDao.getBookmark(url)

    fun insertBookmark(url: String) {
        bookmarksDao.insertBookmark(Bookmark(url))
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

        fun getInstance(bookmarksDao: BookmarkDao, tagDao: TagDao, bookmarkTagPairDao: BookmarkTagPairDao): BookmarksRepository {
            return instance ?: synchronized(this) {
                instance ?: BookmarksRepository(bookmarksDao, tagDao, bookmarkTagPairDao).also { instance = it }
            }
        }
    }
}
