package com.example.bookmarkmanager1.data

//TODO Fetch url title and favicon if they dont exist in database.
class BookmarksRepository private constructor(private val bookmarksDao: BookmarksDao){

    fun getBookmarks() = bookmarksDao.getBookmarks()

    fun getBookmark(url: String) = bookmarksDao.getBookmark(url)

    fun insertBookmark(url: String) {
        bookmarksDao.insertBookmark(Bookmark(url))
    }

    companion object {

        @Volatile private var instance: BookmarksRepository? = null

        fun getInstance(bookmarksDao: BookmarksDao): BookmarksRepository{
            return instance ?: synchronized(this){
                instance ?:BookmarksRepository(bookmarksDao).also { instance = it }
            }
        }
    }
}