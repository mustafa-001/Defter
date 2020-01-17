package com.example.bookmarkmanager1.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Objct for Tag class
 */

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertTag(tag: Tag)

    @Query("DELETE FROM tag WHERE tagname = :tag_")
    fun deleteTagByName(tag_: String)

    @Query("SELECT tId from tag where tagName = :tagName")
    fun getTagId(tagName: String): Int

    @Query("SELECT tagName FROM tag")
    fun getTagNames(): Array<String>

}
