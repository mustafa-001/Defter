package com.ktdefter.defter.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookmarkTagPairDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPair(bookmarkTagPair: BookmarkTagPair)

    @Query("DELETE FROM bookmarktagpair " +
            "WHERE tagid =  :tag " +
            "AND bookmarkid = :url")
    fun deletePair(url: String, tag: String)

    @Query(
        "SElECT b.*  " +
                "FROM  bookmark b, bookmarktagpair bt " +
                "WHERE bt.tagid = :tag " +
                "AND b.url = bt.bookmarkid " +
                "GROUP BY b.url"
    )
    fun getBookmarksWithTag(tag: String): LiveData<List<Bookmark>>

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
