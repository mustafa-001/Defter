package com.ktdefter.defter.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ktdefter.defter.util.getTitleAndFaviconAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch as launch1


@Singleton
class BookmarksRepository @Inject constructor(
    private val bookmarksDao: BookmarkDao,
    private val tagDao: TagDao,
    private val bookmarkTagPairDao: BookmarkTagPairDao,
    @ApplicationContext private val context: Context
) {

    fun syncBookmarks() {
        //Use credential instead of password, ,its save to SharedPreferences by LoginViewModel
//        Firebase.auth.signInWithCredential()
        if (Firebase.auth.currentUser != null) {
            Timber.d("On BookmarksRepository, syncing bookmarks with signed in user: ${Firebase.auth.currentUser!!.uid}")
            val firestoreSync = FirestoreSync(
                this,
                Firebase.firestore,
                Firebase.auth.currentUser!!
            )
            firestoreSync.sync(
                Date(
                    context.getSharedPreferences("SyncSettings", 0).getLong(
                        "lastModificationTime",
                        0
                    )
                )
            )

            context.getSharedPreferences(
                "SyncSettings", 0
            ).edit()
                .putLong("lastModificationTime", Date().time).apply()
        } else {
            Timber.d("authentication is failed")

        }
    }

    fun resetLastSync() {
        context.getSharedPreferences(
            "SyncSettings", 0
        ).edit()
            .putLong("lastModificationTime", 0).apply()
    }

    fun getBookmarks(sortBy: SortBy, sortDirection: SortDirection): LiveData<List<Bookmark>> {


        return bookmarksDao.getBookmarks(sortBy, sortDirection)
//        return bookmarksDao.getBookmarks().map {
//            it.filter { it.favicon == null && false }
//                .map {
//                    GlobalScope.launch {
//                        val newBookmark =
//                            async { getTitleAndFavicon(context, Uri.parse(it.url)) }.await()
//                        Log.d(
//                            "defter",
//                            "There is no saved favicon for ${newBookmark.url}, Trying to fetch."
//                        )
//                        if (newBookmark.favicon == null) {
//                            Log.d("defter", "Cannot fetch favicon for ${newBookmark.url}")
//                        } else {
//                            bookmarksDao.updateBookmark(newBookmark)
//                            Log.d(
//                                "defter",
//                                "Update bookmark is called with ${newBookmark.url} ${newBookmark.favicon}"
//                            )
//                        }
//                    }
//                }
//            return@map it
//        }
    }

    fun getBookmarksSync(): List<Bookmark> = bookmarksDao.getBookmarksSync()
    fun getBookmark(url: String): LiveData<Bookmark?> = bookmarksDao.getBookmark(url)

    suspend fun updateBookmark(
        bookmark: Bookmark,
        fetchTitle: ShouldFetchTitle = ShouldFetchTitle.IfNeeded
    ): Optional<Job>
    {
        bookmarksDao.updateBookmark(bookmark)
        if (fetchTitle == ShouldFetchTitle.Yes || (bookmark.title == null && fetchTitle == ShouldFetchTitle.IfNeeded)) {
                val r = getTitleAndFaviconAsync(context, Uri.parse(bookmark.url))
            return Optional.of(CoroutineScope(Dispatchers.Default).launch1 {  bookmarksDao.updateBookmark(r.await())})
        } else return Optional.empty()
    }

    enum class ShouldFetchTitle {
        Yes, IfNeeded
    }

    fun insertBookmark(bookmark: Bookmark, fetchTitle: ShouldFetchTitle = ShouldFetchTitle.Yes) {
        bookmarksDao.insertBookmark(bookmark)
        Timber.d("Inserting bookmark: $bookmark.url")
        if (fetchTitle == ShouldFetchTitle.Yes) {
            CoroutineScope(Dispatchers.Default).launch1(Dispatchers.Default) {
                fetchMetadataAndUpdateBookmarkAsync(
                    bookmark
                )
            }
        }
    }

    private suspend fun fetchMetadataAndUpdateBookmarkAsync(bookmark: Bookmark) = coroutineScope {
        val r = getTitleAndFaviconAsync(context, Uri.parse(bookmark.url))
        return@coroutineScope launch1 {
            bookmarksDao.updateBookmark(r.await())
        }
    }

    fun fetchMetadata(bookmark: Bookmark): LiveData<Bookmark> {
        val lD: MutableLiveData<Bookmark> = MutableLiveData()
        CoroutineScope(Dispatchers.Default).launch1 {
            val b =
                withContext(Dispatchers.Default) {
                    getTitleAndFaviconAsync(
                        context,
                        Uri.parse(bookmark.url)
                    )
                }
            lD.postValue(b.await())
        }
        return lD
    }

    fun deleteBookmark(url: String) = bookmarksDao.deleteBookmark(url)

    fun insertTag(tag: String) = tagDao.insertTag(Tag(tag))

    fun getTags() = tagDao.getTags()

    fun getTagsSync() = tagDao.getTagsSync()

    fun deleteTag(tagName: String) = tagDao.deleteTag(tagName)

    fun addBookmarkTagPair(url: String, tag: String) {
        bookmarkTagPairDao.addBookmarkTagPair(url, tag)
    }

    fun deleteBookmarkTagPair(url: String, tag: String) =
        bookmarkTagPairDao.deletePair(url, tag)

    fun getTagsOfBookmarkSync(url: String) = bookmarkTagPairDao.getTagsWithBookmarkList(url)

    fun getBookmarksOfTag(tag: Tag, sortBy: SortBy, sortDirection: SortDirection) =
        bookmarkTagPairDao.getBookmarksWithTag(tag, sortBy, sortDirection)

    fun getBookmarksOfTagSync(tag: String) = bookmarkTagPairDao.getBookmarksWithTagSync(tag)
    fun getDeletedBookmarks() = bookmarksDao.getDeletedBookmarks()
    fun getBookmarkSync(url: String): Bookmark? = bookmarksDao.getBookmarkSync(url)

//
//    companion object {
//
//        @Volatile
//        private var instance: BookmarksRepository? = null
//
//        fun getInstance(
//            bookmarksDao: BookmarkDao,
//            tagDao: TagDao,
//            bookmarkTagPairDao: BookmarkTagPairDao,
//            context: Context?
//        ): BookmarksRepository {
//            return instance ?: synchronized(this) {
//                instance ?: BookmarksRepository(
//                    bookmarksDao,
//                    tagDao,
//                    bookmarkTagPairDao,
//                    context
//                ).also { instance = it }
//            }
//        }
//    }
}
