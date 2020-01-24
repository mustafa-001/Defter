package com.ktdefter.defter.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule

class BookmarksDatabaseTest {

//    A JUnit Test Rule that swaps the background executor used by the Architecture Components
//    with a different one which executes each task synchronously.
    @Rule
    @JvmField public val rule = InstantTaskExecutorRule()

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

//        For writing future tests.
//        insBm("test1.com")
//        insBm("test2.com")
//        insBm("test3.com")
//        insTag("test_tag1")
//        insTag("test_tag2")
//        insTag("test_tag3")
//        insPair("test1.com", "test_tag1")
//        insPair("test2.com", "test_tag1")
//        insPair("test3.com", "test_tag1")
//        insPair("test1.com", "test_tag2")
//        insPair("test1.com", "test_tag3")

    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun insBm(url: String = "test.com", title: String = "TestTitle"){
        bookmarkDao.insertBookmark(Bookmark( url, title))
    }

    private fun insTag(tagname: String = "test_tag"){
        tagDao.insertTag(Tag( tagname))
    }

    private fun insPair(url: String, tag: String ){
        bookmarkTagPairDao.addBookmarkTagPair( url, tag)
    }

    @Test
    fun shouldInsertNewBookmark(){
        bookmarkDao.insertBookmark(Bookmark( "test.com", "Test Page"))
        val bm = bookmarkDao.getBookmark("test.com")
        assertNotNull(bm)
    }

    @Test
    fun shouldDeleteBookmark(){
        bookmarkDao.insertBookmark(Bookmark("test.com"))
        bookmarkDao.deleteBookmark("test.com")
        assertNull(bookmarkDao.getBookmark("test.com"))
    }

    @Test
    fun shouldUpdateBookmark(){
        insBm()
        bookmarkDao.getBookmark("test.com").let {
            bookmarkDao
                .insertBookmark(Bookmark(it.url, "New Title"))
            assertEquals(bookmarkDao.getBookmark("test.com").title, "New Title")
        }
    }

    @Test
    fun shouldInsertMultipleBookmark(){
        bookmarkDao.insertBookmark(
            Bookmark( "test1.com", title = "test1title"),
            Bookmark( "test2.com", title = "test2title"))
        bookmarkDao.getBookmark("test2.com").let {
            assertNotNull(it)
            assertEquals(it.title, "test2title")
        }
    }


    @Test
    fun shouldInsertTag(){
        insTag()
        assertNotNull(tagDao.getTags())
    }

    @Test
    fun shouldInsertAndGetMultipleTags(){
        val tag1  = Tag( "tag1")
        val tag2  = Tag( "tag2")
        val tag3  = Tag("tag3")
        tagDao.insertTag(tag1, tag2, tag3)
        tagDao
            .getTags()
            .getValueBlocking()
            .let {
            assertNotNull(it)
            assertTrue( it is List<Tag>)
            assertTrue( it!!.map { it.tagName }.contains("tag2"))
        }
    }

    @Test
    fun shouldDeleteTag(){
        insTag("tagfordelete")

        assertTrue(tagDao
            .getTags()
            .getValueBlocking()
            !!.map { it.tagName }
            .contains("tagfordelete"))
        tagDao.deleteTag("tagfordelete")

        assertFalse(tagDao
            .getTags()
            .getValueBlocking()
            !!.map { it.tagName }
            .contains("tagfordelete"))
    }

    //For now this test is unnecessary
//    @Test
//    fun shouldUpdateTag(){
//        insTag()
//        tagDao
//            .getTags()
//            .getValueBlocking()
//            .also { assertNotNull(it) }!!
//            .filter { it.tagName == "test_tag" }
//            .get(0)
//            .let {oldTag ->
//                tagDao.insertTag(Tag("new tag"))
//                assertEquals(tagDao
//                    .getTags()
//                    .getValueBlocking()
//                    .also { assertNotNull(it) }!!
//                    .filter { oldTag.tagName == it.tagName  }
//                    .get(0)
//                    .tagName, "new tag")
//            }
//    }


    @Test
    fun shouldInsertBookmarkTagPair(){
        insBm("test1.com")
        insTag("test_tag1")
        insPair("test1.com", "test_tag1")
        assert(bookmarkTagPairDao.getTagsWithBookmark("test1.com")
            .getValueBlocking()
            ?.map { it.tagName }
            ?.contains("test_tag1") ?: false
        )
    }


    @Test
    fun shouldGetTagsOfBookmark(){
        insBm("test1.com")
        insTag("test_tag1")
        insTag("test_tag2")
        insTag("test_tag3")
        insPair("test1.com", "test_tag1")
        insPair("test1.com", "test_tag2")
        insPair("test1.com", "test_tag3")

        assertEquals(bookmarkTagPairDao
            .getTagsWithBookmark("test1.com")
            .getValueBlocking()
            ?.map { it.tagName }, listOf("test_tag1", "test_tag2", "test_tag3")
        )
    }

    @Test
    fun shouldGetBookmarksOfTag(){
        insBm("test1.com")
        insBm("test2.com")
        insBm("test3.com")
        insTag("test_tag1")
        insPair("test1.com", "test_tag1")
        insPair("test2.com", "test_tag1")
        insPair("test3.com", "test_tag1")
        assertEquals(bookmarkTagPairDao
            .getBookmarksWithTag("test_tag1")
            .getValueBlocking()
            ?.map { it.url } , listOf("test1.com", "test2.com", "test3.com"))
    }
}