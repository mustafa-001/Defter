package com.example.bookmarkmanager1.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class BookmarksDatabaseTest {

    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var tagDao: TagDao
    private lateinit var bookmarkTagPairDao: BookmarkTagPairDao
    private lateinit var db: BookmarksDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, BookmarksDatabase::class.java).build()
        tagDao = db.tagDao()
        bookmarkDao = db.bookmarkDao()
        bookmarkTagPairDao = db.bookmarkTagPairDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun shouldInsertNewBookmark(){
        bookmarkDao.insertBookmark(Bookmark(0, "test.com", "Test Page", "Some binary gibberish"))
        val bm = bookmarkDao.getBookmark("test.com")
        assertNotNull(bm)
    }

    @Test
    fun shouldDeleteBookmark(){
        bookmarkDao.insertBookmark(Bookmark(0,"test.com"))
        bookmarkDao.deleteBookmark("test.com")
        assertNull(bookmarkDao.getBookmark("test.com"))
    }

    @Test
    fun shouldInsertMultipleBookmark(){
        bookmarkDao.insertBookmark(
            Bookmark(0, "test1.com", title = "test1title"),
            Bookmark(0, "test2.com", title = "test2title"))
        assertNotNull(bookmarkDao.getBookmark("test2.com"))
        assertEquals(bookmarkDao.getBookmark("test2.com").title, "test2title")
    }


//
    @Test
    fun shouldInsertTag(){
        tagDao.insertTag(Tag(0, "tag1"))
        assertNotNull(tagDao.getTagNames())
    }

    @Test
    fun shouldInsertAndGetMultipleTags(){
        val tag1  = Tag(0, "tag1")
        val tag2  = Tag(0, "tag2")
        val tag3  = Tag(0, "tag3")
        tagDao.insertTag(tag1, tag2, tag3)
        val tags = tagDao.getTagNames()
        assertNotNull(tags)
        assertTrue("should return list",tags is Array<String>)
        assertTrue("Returned list should contain strings", tags.get(0) is String)
        for (t in tags)
            Log.d("tags :", t)
        assertTrue("Should contain our tag", tags.contains("tag3"))
    }

    @Test
    fun shouldDeleteTag(){
        tagDao.insertTag(Tag(0, "tagfordelete"))
        assertNotNull(tagDao.getTagNames().contains("tagfordelete"))
        tagDao.deleteTagByName("tagfordelete")
        assertFalse(tagDao.getTagNames().contains("tagfordelete"))
    }


    @Test
    fun shouldInsertBookmarkTagPair(){
        bookmarkDao.insertBookmark(Bookmark(0, "test50.com"))
        tagDao.insertTag(Tag(0,"tag50"))
        bookmarkTagPairDao.addBookmarkTagPair("test50.com", "tag50")
    }

    @Test
    fun shouldGetTagsOfBookmark(){
        bookmarkDao.insertBookmark(Bookmark(0, "test10.com"))
        tagDao.insertTag(Tag(0,"tag10"), Tag(0, "tag11"))
        bookmarkTagPairDao.addBookmarkTagPair("test10.com", "tag10")
        bookmarkTagPairDao.addBookmarkTagPair("test10.com", "tag11")
//        val observer = Observer<Array<String>> {
//            assertArrayEquals(it, arrayOf("tag10", "tag11"))
//        }
//        bookmarkTagPairDao.getTagsWithBookmark("test10.com").observe(this, observer)
    }
    @Test
    fun bookmarkDao() {
    }

    @Test
    fun tagDao() {
    }

    @Test
    fun bookmarkTagPairDao() {
    }
}