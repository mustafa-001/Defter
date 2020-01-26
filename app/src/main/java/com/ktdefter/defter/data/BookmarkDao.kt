package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 *Data Access Object for Bookmark class
 */
@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(vararg bookmark: Bookmark)

    @Query("SELECT * FROM bookmark")
    fun getBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE url = :url")
    fun getBookmark(url: String): Bookmark

    @Query("DELETE FROM bookmark WHERE url = :url")
    fun deleteBookmark(url: String)
}
