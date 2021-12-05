package com.ktdefter.defter.data

import android.os.Build
import android.text.style.TtsSpan
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.type.DateTimeOrBuilder
import okhttp3.internal.wait
import timber.log.Timber
import java.time.Instant
import java.util.*
import javax.inject.Inject

class FirestoreSync @Inject constructor(
    private val bookmarksDao: BookmarkDao,
    private val tagDao: TagDao,
    private val bookmarkTagPairDao: BookmarkTagPairDao,
    private val firestoreDatabase: FirebaseFirestore
) {

    fun pushBookmark(bookmark: Bookmark): Task<Void> {
        Timber.d("Pushing bookmark ${bookmark.url} to Firestore")
        return firestoreDatabase.collection("bookmarks")
            .document(bookmark.url.replace("/", "_slash_")).set(bookmark)
    }

    fun getBookmarksFromLocalSince(lastSyncDate: Date) {

    }

    fun getBookmarksFromRemoteSince(lastSyncDate: Date) {


    }

    fun sync(lastSyncDate: Date) {

        Timber.d("on sync to Firestore with lastsynctime of $lastSyncDate")
        val bookmarksFromLocal = mutableSetOf<Bookmark>()
        bookmarksFromLocal.addAll(bookmarksDao.getBookmarksSync().filter { it.lastModification > lastSyncDate })
        val bookmarksFromRemote = mutableSetOf<Bookmark>()

        val task = firestoreDatabase.collection("bookmarks")
            .whereGreaterThan("last_modification", Timestamp(lastSyncDate))
            .get()
        while (!task.isSuccessful) {
        }
        task.result!!.documents.let {
            it.forEach { bookmark ->
                bookmarksFromRemote.add(
                    Bookmark(
                        (bookmark.data!!.get("url") as String),
                        lastModification = ((bookmark.data?.get("last_modification")
                            ?: Timestamp(
                                Date()
                            )) as Timestamp).toDate()
                    )
                )
            }
        }

        val needToPull = bookmarksFromRemote - bookmarksFromLocal
        val needToPush = bookmarksFromLocal.subtract( bookmarksFromRemote)
        val conflicts = bookmarksFromLocal.intersect(bookmarksFromRemote)

        needToPull.forEach {
            bookmarksDao.insertBookmark(it)
            Timber.d("found new ${it.url} on remote, inserting to local")
        }
        needToPush.forEach { pushBookmark(it) }
        conflicts.forEach { c ->
            val bLocal = bookmarksFromLocal.find { it.url == c.url }!!
            val bRemote = bookmarksFromRemote.find { it.url == c.url }!!
            if (bLocal.lastModification > bRemote.lastModification) {
                pushBookmark(bLocal)
            } else {
                bookmarksDao.insertBookmark(bRemote)
            }
        }
    }

}