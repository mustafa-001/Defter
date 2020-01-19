package com.ktdefter.defter.data

//TODO Fetch url title and favicon if they don't exist in database.
class BookmarksRepository private constructor(
    private val bookmarksDao: BookmarkDao,
    private val tagDao: TagDao,
    private val bookmarkTagPairDao: BookmarkTagPairDao){

    fun getBookmarks() = bookmarksDao.getBookmarks()

    fun getBookmark(url: String) = bookmarksDao.getBookmark(url)

    fun insertBookmark(url: String) {
        bookmarksDao.insertBookmark(Bookmark(null, url))
    }

    fun deleteBookmark(url: String){
        bookmarksDao.deleteBookmark(url)
    }

    fun addTag(tag: String){
        val t = Tag(tId = 0, tagName = tag)
        tagDao.insertTag(t)
    }

    fun getTags() = tagDao.getTags()

    fun deleteTag(tagName: String){
        tagDao.deleteTagByName(tagName)
    }

    fun addBookmarkTagPair(url: String, tag: String){
        bookmarkTagPairDao.addBookmarkTagPair(url, tag)
    }

    companion object {

        @Volatile private var instance: BookmarksRepository? = null

        fun getInstance(bookmarksDao: BookmarkDao, tagDao: TagDao, bookmarkTagPairDao: BookmarkTagPairDao): BookmarksRepository{
            return instance ?: synchronized(this){
                instance ?:BookmarksRepository(bookmarksDao, tagDao, bookmarkTagPairDao).also { instance = it }
            }
        }
    }
}