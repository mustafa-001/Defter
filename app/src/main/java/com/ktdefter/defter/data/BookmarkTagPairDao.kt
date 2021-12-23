package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookmarkTagPairDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPair(bookmarkTagPair: BookmarkTagPair)

    @Query(
        "DELETE FROM bookmarktagpair " +
                "WHERE tagid =  :tag " +
                "AND bookmarkid = :url"
    )
    fun deletePair(url: String, tag: String)

    @Query(
        "SElECT b.*  " +
                "FROM  bookmark b, bookmarktagpair bt " +
                "WHERE bt.tagid = :tag " +
                "AND b.url = bt.bookmarkid " +
                "GROUP BY b.url ORDER BY " +
                "CASE WHEN :sortBy = 'hostname' AND  :sortDirection = 'asc'  THEN b.hostname END ASC," +
                "CASE WHEN :sortBy = 'hostname' AND :sortDirection = 'desc'  THEN  b.hostname END DESC," +
                "CASE WHEN :sortBy = 'lastModification' AND :sortDirection = 'asc'   THEN b.lastModification END ASC," +
                "CASE WHEN :sortBy = 'lastModification' AND :sortDirection = 'desc'    THEN b.lastModification END DESC"
    )
    fun getBookmarksWithTagImpl(
        tag: String,
        sortBy: String,
        sortDirection: String
    ): LiveData<List<Bookmark>>

    fun getBookmarksWithTag(
        tag: Tag,
        sortBy: SortBy = SortBy.MODIFICATION_TIME,
        sortDirection: SortDirection = SortDirection.DESC
    ): LiveData<List<Bookmark>> {
        return getBookmarksWithTagImpl(tag.tagName, sortBy.string, sortDirection.string)
    }

    @Query(
        "SElECT b.*  " +
                "FROM  bookmark b, bookmarktagpair bt " +
                "WHERE bt.tagid = :tag " +
                "AND b.url = bt.bookmarkid " +
                "GROUP BY b.url"
    )
    fun getBookmarksWithTagSync(tag: String): List<Bookmark>

    @Query(
        "SELECT t.*  " +
                "FROM tag t, bookmarktagpair bt " +
                "WHERE bt.bookmarkid = :url " +
                "AND bt.tagid = t.tagname " +
                "GROUP BY t.tagName"
    )
    fun getTagsWithBookmark(url: String): LiveData<List<Tag>>

    @Query(
        "SELECT t.*  " +
                "FROM tag t, bookmarktagpair bt " +
                "WHERE bt.bookmarkid = :url " +
                "AND bt.tagid = t.tagname " +
                "GROUP BY t.tagName"
    )
    fun getTagsWithBookmarkList(url: String): List<Tag>

    @Query(
        "INSERT INTO bookmarktagpair values (null, :url, :tag) "
    )
    fun addBookmarkTagPair(url: String, tag: String)
}
