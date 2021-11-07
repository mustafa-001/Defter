package com.ktdefter.defter.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.ktdefter.defter.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(val bookmarksRepository: BookmarksRepository) :
    ViewModel() {
    private var position = 0

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
                    mlv.addSource(t) {
                        mlv.value =
                            if (searchKeyword.isPresent)
                                it.filter { it.url.contains(searchKeyword.get()) }
                            else it

                    }
                    queryParametersChanged.postValue(false)
                }
            }
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

    fun updateBookmark(
        oldBookmark: Bookmark,
        newBookmark: Bookmark,
        fetchTitle: BookmarksRepository.ShouldFetchTitle = BookmarksRepository.ShouldFetchTitle.IfNeeded
    ) {
        if (oldBookmark.url != newBookmark.url) {
            deleteBookmark(oldBookmark.url)
            addBookmark(newBookmark)
        }

        bookmarksRepository.updateBookmark(newBookmark, fetchTitle)
    }

    fun getBookmarksSync(): List<Bookmark> {
        return bookmarksRepository.getBookmarksSync().map {
            it.apply {
                it.tags = bookmarksRepository.getTagsOfBookmarkSync(it.url)
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
            Log.d("Defter", "adding new $tag")
        }

        Log.d("Defter", "adding tag $tag to $url")
        bookmarksRepository.addBookmarkTagPair(url, tag)
    }

    fun deleteBookmarkTagPair(url: String, tag: String) {
        bookmarksRepository.deleteBookmarkTagPair(url, tag)
        if (getBookmarksOfTagSync(tag).isEmpty()) {
            this.bookmarksRepository.deleteTag(tag)
            Log.d("Defter", "Tag $tag doesn't have any related bookmark, it is deleted")
        }
    }
    fun getBookmark(url: String): Bookmark? = bookmarksRepository.getBookmark(url)

    fun getTags(): LiveData<List<Tag>> = bookmarksRepository.getTags()

    fun getTagsSync(): List<Tag> = bookmarksRepository.getTagsSync()

    fun getTagsOfBookmark(url: String) = bookmarksRepository.getTagsOfBookmark(url)

    fun getTagsOfBookmarkSync(url: String) = bookmarksRepository.getTagsOfBookmarkSync(url)


    fun getBookmarksOfTagSync(tag: String) = bookmarksRepository.getBookmarksOfTagSync(tag)
}
