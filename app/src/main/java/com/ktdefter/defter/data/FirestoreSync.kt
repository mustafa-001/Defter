package com.ktdefter.defter.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import timber.log.Timber
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
            .document(removeSlashes(bookmark.url)).set(bookmark)
    }

    fun removeSlashes(url: String): String {
        return url.replace("/", "_slash_")
    }

    fun getBookmarksFromLocalSince(lastSyncDate: Date) {

    }

    fun getBookmarksFromRemoteSince(lastSyncDate: Date) {


    }

    fun taskToBookmarks(task: Task<QuerySnapshot>): List<Bookmark> {
        while (!task.isSuccessful) {
        }
        return task.result!!.documents.map {
            Bookmark(
                (it.data!!.get("url") as String),
                lastModification = ((it.data?.get("lastModification")
                    ?: Timestamp(
                        Date()
                    )) as Timestamp).toDate(),
                isDeleted = ((it.data!!.get("isDeleted") as Boolean))
            )
        }.toList()
    }

    enum class BookmarkConflictResolution() {
        FIRST,
        SECOND,
        DELETE_BOTH
    }

    fun handleConflict(b1: Bookmark, b2: Bookmark): BookmarkConflictResolution {
        if (!b1.isDeleted || !b2.isDeleted) {
            if (b1.lastModification > b2.lastModification) {
                return if (b1.isDeleted) BookmarkConflictResolution.DELETE_BOTH else BookmarkConflictResolution.FIRST
            } else {
                return if (b2.isDeleted) BookmarkConflictResolution.DELETE_BOTH else BookmarkConflictResolution.SECOND
            }
        }

        return BookmarkConflictResolution.DELETE_BOTH
    }

    fun sync(lastSyncDate: Date) {

        Timber.d("on sync to Firestore with lastsynctime of $lastSyncDate")
        val bookmarksFromLocal = mutableSetOf<Bookmark>()
        bookmarksFromLocal.addAll(
            bookmarksDao.getBookmarksSync().filter { it.lastModification > lastSyncDate })

        val deletedBookmarksOnLocal = bookmarksDao.getDeletedBookmarks().toSet()

        val deletedBookmarksOnRemote =
            taskToBookmarks(
                firestoreDatabase.collection("bookmarks")
                    .whereGreaterThan("lastModification", Timestamp(lastSyncDate))
                    .whereEqualTo("isDeleted", true)
                    .get()
            ).toSet()
//            .forEach { b ->
//                firestoreDatabase.collection("bookmarks").document(removeSlashes(b.url))
//                    .update("isDeleted", true)
//                //No need to wait for response from Firestore, as it will mark it on local copy in case of
//                // not having network connection.
//            }

        val bookmarksFromRemote = mutableSetOf<Bookmark>()
        val task = firestoreDatabase.collection("bookmarks")
            .whereGreaterThan("lastModification", Timestamp(lastSyncDate))
            .whereEqualTo("isDeleted", false)
            .get()

        bookmarksFromRemote.addAll(taskToBookmarks(task))

        val needToPull: Set<Bookmark> =
            (bookmarksFromRemote.subtract(bookmarksFromLocal)).subtract(deletedBookmarksOnRemote)
        val needToPush: Set<Bookmark> =
            (bookmarksFromLocal.subtract(bookmarksFromRemote)).subtract(deletedBookmarksOnLocal)
        val conflicts: Map<Bookmark, Bookmark> = bookmarksFromLocal.intersect(bookmarksFromRemote)
            .map { fromLocal ->
                return@map (fromLocal to bookmarksFromRemote.filter { it.url == fromLocal.url }
                    .first())
            }
            .toMap()

        needToPull.forEach {
            bookmarksDao.insertBookmark(it)
            Timber.d("found new ${it.url} on remote, inserting to local")
        }
        needToPush.forEach {
            pushBookmark(it)
            Timber.d("found new ${it.url} on local, inserting to remote")
        }
        conflicts.forEach { (c1, c2: Bookmark) ->
            val res = handleConflict(c1, c2)
            when (res) {
                BookmarkConflictResolution.DELETE_BOTH -> {
                    bookmarksDao.deleteBookmark(c1.url)
                    firestoreDatabase.collection("bookmarks").document(removeSlashes(c2.url))
                        .update("isDeleted", true)
                }
                BookmarkConflictResolution.FIRST -> firestoreDatabase.collection("bookmarks")
                    .document(removeSlashes(c1.url)).set(c1)
                BookmarkConflictResolution.SECOND -> bookmarksDao.updateBookmark(c2)
            }
            Timber.d("Bookmark sync ocnflict, url: ${c1.url},  resolving to $res")
        }

    }
}