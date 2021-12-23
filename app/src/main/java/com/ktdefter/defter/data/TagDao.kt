package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.*
import javax.inject.Singleton

/**
 * Data Access Object for Tag class
 */

@Dao
@Singleton
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertTag(vararg tag: Tag)

    @Query("DELETE FROM tag WHERE tagname = :tag")
    fun deleteTag(tag: String)

    @Query("SELECT * FROM tag")
    fun getTags(): LiveData<List<Tag>>

    @Query("SELECT * FROM tag")
    fun getTagsSync(): List<Tag>
}
