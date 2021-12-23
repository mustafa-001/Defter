package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import javax.inject.Singleton

/**
 *Data Access Object for Bookmark class
 */
@Dao
@Singleton
interface BookmarkDao {
    // TODO Don't Replace on Conflict, rather update the old
//    item. Replacing causes old items to get deleted from Recycler View
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(vararg bookmark: Bookmark)

    @Update
    fun updateBookmark(vararg bookmark: Bookmark)


    //TODO Tidy up and nest these CASE's
    @Query(
        "SELECT * FROM bookmark WHERE isDeleted = 0 ORDER BY " +
                "CASE WHEN :sortBy = 'hostname' AND :sortDirection = 'asc'  THEN hostname END ASC," +
                "CASE WHEN :sortBy = 'hostname' AND :sortDirection = 'desc'  THEN  hostname END DESC," +
                "CASE WHEN :sortBy = 'lastModification' AND :sortDirection = 'asc' THEN lastModification END ASC," +
                "CASE WHEN :sortBy = 'lastModification' AND :sortDirection = 'desc' THEN lastModification END DESC"

    )
    fun _getBookmarksImpl(sortBy: String, sortDirection: String): LiveData<List<Bookmark>>

    fun getBookmarks(
        sortBy: SortBy = SortBy.MODIFICATION_TIME,
        sortDirection: SortDirection = SortDirection.DESC
    ): LiveData<List<Bookmark>> {
        return _getBookmarksImpl(sortBy.string, sortDirection.string)
    }

    @Query("SELECT * FROM bookmark WHERE isDeleted = 0")
    fun getBookmarksSync(): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE isDeleted = 1")
    fun getDeletedBookmarks(): List<Bookmark>


    @Query("SELECT * FROM bookmark WHERE url = :url")
    fun getBookmark(url: String): LiveData<Bookmark?>

    @Query("DELETE FROM bookmark WHERE url = :url")
    fun deleteBookmark(url: String)
}

enum class SortDirection(val string: String) {
    ASC("asc"),
    DESC("desc")
}

enum class SortBy(val string: String) {
    MODIFICATION_TIME("lastModification"),
    HOSTNAME("hostname")
}
