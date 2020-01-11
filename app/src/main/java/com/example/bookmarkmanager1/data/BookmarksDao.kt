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
interface BookmarksDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(vararg bookmarks: Bookmark)

    @Query("SELECT * FROM bookmark")
    fun getBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE url = :url")
    fun getBookmark(url: String): LiveData<Bookmark>

}
