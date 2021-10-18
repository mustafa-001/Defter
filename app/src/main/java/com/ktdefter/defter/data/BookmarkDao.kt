package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import javax.inject.Inject

/**
 *Data Access Object for Bookmark class
 */
@Dao
interface BookmarkDao  {
    // TODO Don't Replace on Conflict, raher update the old
//    item. Replacing causes old items to get deleted from Recycler View
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(vararg bookmark: Bookmark)

    @Query("SELECT * FROM bookmark")
    fun getBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark")
    fun getBookmarksSync(): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE url = :url")
    fun getBookmark(url: String): Bookmark?

    @Query("DELETE FROM bookmark WHERE url = :url")
    fun deleteBookmark(url: String)
}
