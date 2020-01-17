package com.example.bookmarkmanager1.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 *Data Access Object for Bookmark class
 */
@Dao
interface BookmarkDao{
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM bookmark")
    fun getBookmarks(): LiveData<Array<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE url = :url")
    fun getBookmark(url: String): Bookmark

    @Query("select bId from bookmark where url = :url")
    fun getBookmarkId(url: String): Int

    @Query("DELETE FROM bookmark WHERE url = :url")
    fun deleteBookmark(url: String)

}
