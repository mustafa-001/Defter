package com.example.bookmarkmanager1.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Objct for Tag class
 */

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTag(vararg tag: Tag)

    @Query("DELETE FROM tag WHERE tagname = :tag_")
    fun deleteTagByName(tag_: String)

    @Query("SELECT tagName FROM tag")
    fun getTagNames(): Array<String>

}
