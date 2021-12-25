package com.ktdefter.defter.viewmodels

import androidx.lifecycle.*
import com.ktdefter.defter.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.sql.Time
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(val bookmarksRepository: BookmarksRepository) :
    ViewModel() {
    //This repetition is better than 60+ line, 4 MutableLiveData and addSource() mess.
    //That also means we Observe parameters we set, Which I think is schizophrenic.
    private val queryParametersChanged = MutableLiveData(true)
    var sortBy: SortBy = SortBy.MODIFICATION_TIME
        set(value) {
            field = value
            queryParametersChanged.postValue(true)
        }

    var sortDirection: SortDirection = SortDirection.DESC
        set(value) {
            field = value
            queryParametersChanged.postValue(true)
        }

    var tag: Optional<Tag> = Optional.empty()
        set(value) {
            field = value
            queryParametersChanged.postValue(true)
        }
    var searchKeyword: Optional<String> = Optional.empty()
        set(value) {
            field = value
            queryParametersChanged.postValue(true)
        }
    val bookmarksToShow: MediatorLiveData<List<Bookmark>> =
        MediatorLiveData<List<Bookmark>>().also { mlv ->
            mlv.addSource(queryParametersChanged) {
                if (it) {
                    val t: LiveData<List<Bookmark>> = if (tag.isPresent) {
                        bookmarksRepository.getBookmarksOfTag(
                            tag.get(),
                            sortBy,
                            sortDirection
                        )
                    } else {
                        bookmarksRepository.getBookmarks(sortBy, sortDirection)
                    }
                    mlv.addSource(t) { it1 ->
                        mlv.value =
                            if (searchKeyword.isPresent)
                                it1.filter { it2t -> it2t.url.contains(searchKeyword.get()) }
                            else it1

                    }
                    queryParametersChanged.postValue(false)
                }
            }
        }

    data class DownloadStatus(
        var isDowloading: Boolean = false,
        var maxDownloads: Int = 0,
        var currentDownloads: Int = 0
    )

    val downloadStatus = MutableLiveData<DownloadStatus>()

    fun markBookmarkSelected(bookmark: Bookmark) {
        val b: List<Bookmark> = bookmarksToShow.value?.map {
            if (it.url == bookmark.url) {
                val c = it.copy()
                c.isSelected = true
                c
            } else {
                it
            }
        } as List<Bookmark>
        bookmarksToShow.postValue(b)
    }

    fun markBookmarkUnselected(bookmark: Bookmark) {
        val b: List<Bookmark> = bookmarksToShow.value?.map {
            if (it.url == bookmark.url) {
                val c = it.copy()
                c.isSelected = false
                c
            } else {
                it
            }
        } as List<Bookmark>
        bookmarksToShow.postValue(b)
    }

/*  fun getBookmarks(
      sortBy: MutableLiveData<SortBy> = MutableLiveData(SortBy.MODIFICATION_TIME),
      sortDirection: SortDirection = SortDirection.DESC
  ): MediatorLiveData<List<Bookmark>> {
      return MediatorLiveData<List<Bookmark>>().let { mlv ->
          mlv.addSource(searchKeyword) { keyword ->
              if (keyword.isPresent) {
                  //Remove it in case it's added in other branch. It throws
                  // java.lang.IllegalArgumentException: This source was already added with the different observer
                  mlv.removeSource(tag)
                  Log.d("defter", "filtering")
                  mlv.addSource(tag) { tag ->
                      if (tag.isPresent) {
                          mlv.addSource(
                              bookmarksRepository.getBookmarksOfTag(
                                  tag.get(),
                                  sortBy.value!!,
                                  sortDirection
                              )
                          ) { bookmarks ->
                              mlv.value =
                                  bookmarks.filter { it.url.contains(keyword.get()) }
                          }
                      } else {
                          mlv.addSource(
                              bookmarksRepository.getBookmarks(
                                  sortBy.value!!,
                                  sortDirection
                              )
                          ) { bookmarks ->
                              mlv.value =
                                  bookmarks.filter { it.url.contains(keyword.get()) }
                          }
                      }
                  }
              } else {
                  Log.d("defter", "not filtering")
                  mlv.removeSource(tag)
                  mlv.addSource(tag) { tag ->
                      if (tag.isPresent) {
                          mlv.addSource(
                              bookmarksRepository.getBookmarksOfTag(
                                  tag.get(),
                                  sortBy,
                                  sortDirection
                              )
                          ) { bookmarks ->
                              mlv.value = bookmarks
                          }
                      } else {
                          mlv.addSource(
                              bookmarksRepository.getBookmarks(
                                  sortBy,
                                  sortDirection
                              )
                          ) { bookmarks ->
                              mlv.value = bookmarks
                          }
                      }
                  }
              }
          }
          mlv
      }
  }*/

    fun replaceBookmark(
        oldBookmark: Bookmark,
        newBookmark: Bookmark,
        fetchTitle: BookmarksRepository.ShouldFetchTitle = BookmarksRepository.ShouldFetchTitle.IfNeeded
    ) {
        if (oldBookmark.url != newBookmark.url) {
            addBookmark(newBookmark)
            deleteBookmark(oldBookmark.url)
            return
        }

        CoroutineScope(Dispatchers.Default).launch {  bookmarksRepository.updateBookmark(newBookmark, fetchTitle)}
    }

    fun getBookmarksSync(): List<Bookmark> {
        return bookmarksRepository.getBookmarksSync().map {
            it.apply {
                it.tags = bookmarksRepository.getTagsOfBookmarkSync(it.url)
            }
        }
    }

    fun downloadMissingMetadataForAll() {
        val bookmarksToDownload = getBookmarksSync()
        downloadStatus.value = DownloadStatus(true, bookmarksToDownload.size, 0)
        var maxD = 0
        var currentD = 0
        for ((idx, b) in bookmarksToDownload.withIndex()) {
            CoroutineScope(Dispatchers.Default).launch {
                val job = bookmarksRepository.updateBookmark(
                    b,
                    BookmarksRepository.ShouldFetchTitle.IfNeeded
                )
                if (job.isPresent){
                    if (b.title != null){
                        throw Exception("Should be unreachable")
                    }
                    Timber.d("Got a Update Bookmark Info Job with $b, adding it to downloadStatus")
                    maxD +=1
                    downloadStatus.postValue( DownloadStatus(true, maxD, currentD))
                    job.get().invokeOnCompletion {
                        if (bookmarksRepository.getBookmarkSync(b.url)!!.title == null){
                            Timber.d("Bookmark info failed to be  updated $b")
                        } else {
                            Timber.d("Bookmark info is updated for $b")
                            currentD += 1
                            downloadStatus.postValue( DownloadStatus(true, maxD, currentD))
                        }
                    }
                } else {

                    Timber.d("No Job returned from updateBookmark with $b")
                }
            }
        }
    }

    fun addBookmark(
        bookmark: Bookmark,
        fetchTitle: BookmarksRepository.ShouldFetchTitle = BookmarksRepository.ShouldFetchTitle.Yes
    ) = bookmarksRepository.insertBookmark(bookmark, fetchTitle)

    fun deleteBookmark(url: String) {
        bookmarksRepository.deleteBookmark(url)
    }

    fun addBookmarkTagPair(url: String, tag: String) {
        if (!bookmarksRepository
                .getTagsSync()
                .map { it.tagName }
                .contains(tag)
        ) {
            bookmarksRepository.insertTag(tag)
            Timber.d("adding new $tag")
        }

        Timber.d("adding tag $tag to $url")
        bookmarksRepository.addBookmarkTagPair(url, tag)
    }

    fun deleteBookmarkTagPair(url: String, tag: String) {
        bookmarksRepository.deleteBookmarkTagPair(url, tag)
        if (getBookmarksOfTagSync(tag).isEmpty()) {
            this.bookmarksRepository.deleteTag(tag)
            Timber.d("Tag $tag doesn't have any related bookmark, it is deleted")
        }
    }

    fun getBookmark(url: String): LiveData<Bookmark?> = bookmarksRepository.getBookmark(url)

    fun getTags(): LiveData<List<Tag>> = bookmarksRepository.getTags()

    fun getTagsSync(): List<Tag> = bookmarksRepository.getTagsSync()

    fun getTagsOfBookmarkSync(url: String) = bookmarksRepository.getTagsOfBookmarkSync(url)

    private fun getBookmarksOfTagSync(tag: String) = bookmarksRepository.getBookmarksOfTagSync(tag)
}
