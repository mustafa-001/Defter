package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Objct for Tag class
 */

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTag(vararg tag: Tag)

    @Query("DELETE FROM tag WHERE tagname = :tag")
    fun deleteTagByName(tag: String)

    @Query("SELECT tId from tag where tagName = :tagName")
    fun getTagId(tagName: String): Int

    @Query("SELECT * FROM tag")
    fun getTags(): LiveData<List<Tag>>

}
