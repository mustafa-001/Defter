package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 *Data Access Object for Bookmark class
 */
@Dao
@Singleton
interface BookmarkDao  {
    // TODO Don't Replace on Conflict, raher update the old
//    item. Replacing causes old items to get deleted from Recycler View
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertBookmark(vararg bookmark: Bookmark)

    @Update
    fun updateBookmark(vararg bookmark: Bookmark)

    @Query("SELECT * FROM bookmark")
    fun getBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark")
    fun getBookmarksSync(): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE url = :url")
    fun getBookmark(url: String): Bookmark?

    @Query("DELETE FROM bookmark WHERE url = :url")
    fun deleteBookmark(url: String)
}
