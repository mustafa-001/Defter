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
interface BookmarkDao {
    // TODO Don't Replace on Conflict, rather update the old
//    item. Replacing causes old items to get deleted from Recycler View
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertBookmark(vararg bookmark: Bookmark)

    @Update
    fun updateBookmark(vararg bookmark: Bookmark)

    @Query("SELECT * FROM bookmark ORDER BY lastModification  DESC")
    fun getBookmarksSortByLastModificationDesc(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark ORDER BY  lastModification ASC")
    fun getBookmarksSortByLastModificationAsc(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark ORDER BY hostname  DESC")
    fun getBookmarksSortByHostnameDesc(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark ORDER BY hostname  ASC")
    fun getBookmarksSortByHostnameAsc(): LiveData<List<Bookmark>>

    fun getBookmarks(
        sortBy: SortBy = SortBy.MODIFICATION_TIME,
        sortDirection: SortDirection = SortDirection.DESC
    ): LiveData<List<Bookmark>> {
        if (sortBy == SortBy.MODIFICATION_TIME && sortDirection == SortDirection.ASC) {
            return getBookmarksSortByLastModificationAsc()
        } else if (sortBy == SortBy.HOSTNAME && sortDirection == SortDirection.DESC) {
            return getBookmarksSortByHostnameDesc()
        } else if (sortBy == SortBy.HOSTNAME && sortDirection == SortDirection.ASC) {
            return getBookmarksSortByHostnameAsc()
        } else if (sortBy == SortBy.MODIFICATION_TIME && sortDirection == SortDirection.DESC) {
            return getBookmarksSortByLastModificationDesc()
        } else {
            return getBookmarksSortByLastModificationDesc()
        }
    }

    @Query("SELECT * FROM bookmark")
    fun getBookmarksSync(): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE url = :url")
    fun getBookmark(url: String): Bookmark?

    @Query("DELETE FROM bookmark WHERE url = :url")
    fun deleteBookmark(url: String)
}

enum class SortDirection(val s: String) {
    ASC("asc"),
    DESC("desc")
}

enum class SortBy(val s: String) {
    MODIFICATION_TIME("lastModification"),
    HOSTNAME("hostname")
}
