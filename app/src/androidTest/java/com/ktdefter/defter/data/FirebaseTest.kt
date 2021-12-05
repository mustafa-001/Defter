package com.ktdefter.defter.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import java.sql.Time
import java.time.Instant
import java.util.*

class FirebaseTest {

    //    A JUnit Test Rule that swaps the background executor used by the Architecture Components
//    with a different one which executes each task synchronously.
    @Rule
    @JvmField
    public val rule = InstantTaskExecutorRule()

    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var tagDao: TagDao
    private lateinit var bookmarkTagPairDao: BookmarkTagPairDao
    private lateinit var db: BookmarksDatabase
    private lateinit var syncer: FirestoreSync
    private val mockFirestore: FirebaseFirestore = mock(FirebaseFirestore.class, "mockFirestore")
    private val lastSyncTime = Date()
    private val aDayBeforeLastSync: Date =
        Date.from(lastSyncTime.toInstant().plusMillis(-1000 * 60 * 60 * 24))
    val bookmarksOnLocal = listOf<Bookmark>(
        Bookmark("onboth1.com", lastModification = aDayBeforeLastSync),
        Bookmark("onboth2.com"),
        Bookmark("onlocal1.com"),
        Bookmark("deletedonlocalsincelastsync.com", isDeleted = true)
    )
    val bookmarksOnFirestore = listOf<Bookmark>(
        Bookmark("onremote1"),
        Bookmark("onboth1.com", lastModification = aDayBeforeLastSync),
        Bookmark("onboth2.com"),
        Bookmark("deletedonremotesincelastsync.com", isDeleted = true)
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, BookmarksDatabase::class.java
        ).build()
        tagDao = db.tagDao()
        bookmarkDao = db.bookmarkDao()
        bookmarkTagPairDao = db.bookmarkTagPairDao()
        syncer = FirestoreSync(db, mockFirestore)
        when (mockFirestore.collection("bookmarks"))
    }

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

    @Test
    fun shouldPushBookmark() {
        syncer.pushBookmark(Bookmark("bardak.com"))
    }

    @After
    fun tearDown() {
        db.close()
    }
}