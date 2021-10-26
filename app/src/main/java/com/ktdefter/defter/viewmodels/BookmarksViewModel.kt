package com.ktdefter.defter.viewmodels

import android.app.Application
import android.graphics.Path
import android.util.Log
import androidx.lifecycle.*
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.data.Tag
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class BookmarksViewModel @Inject constructor(val bookmarksRepository: BookmarksRepository) :
    ViewModel() {
    private var position = 0
    var tag: MutableLiveData<Optional<Tag>> = MutableLiveData(Optional.empty())
    val searchKeyword: MutableLiveData<Optional<String>> =
        MutableLiveData(Optional.empty())
    val bookmarksToShow: MediatorLiveData<List<Bookmark>> = getBookmarks()

    // TODO Add tags witch switchmap to bookmarks, dont observer Livedata<List<Tag>>
    // from Adapter
    private fun getBookmarks(): MediatorLiveData<List<Bookmark>> {
        return MediatorLiveData<List<Bookmark>>().let { mlv ->
            mlv.addSource(searchKeyword) { keyword ->
                if (keyword.isPresent) {
                    //Remove it in case it's added in other branch. It throws
                    // java.lang.IllegalArgumentException: This source was already added with the different observer
                    mlv.removeSource(tag)
                    Log.d("defter", "filtering")
                    mlv.addSource(tag) { tag ->
                        if (tag.isPresent) {
                            mlv.addSource(bookmarksRepository.getBookmarksOfTag(tag.get().tagName)) { bookmarks ->
                                mlv.value =
                                    bookmarks.filter { it.url.contains(keyword.get()) }
                            }
                        } else {
                            mlv.addSource(bookmarksRepository.getBookmarks()) { bookmarks ->
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
                            mlv.addSource(bookmarksRepository.getBookmarksOfTag(tag.get().tagName)) { bookmarks ->
                                mlv.value = bookmarks
                            }
                        } else {
                            mlv.addSource(bookmarksRepository.getBookmarks()) { bookmarks ->
                                mlv.value = bookmarks
                            }
                        }
                    }
                }
            }
            mlv
        }
    }

    fun getBookmarksSync(): List<Bookmark> {
        return bookmarksRepository.getBookmarksSync().map {
            it.apply {
                it.tags = bookmarksRepository.getTagsOfBookmarkSync(it.url)
            }
        }
    }

    fun addBookmark(url: String) {
        bookmarksRepository.insertBookmark(url)
    }

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

    fun getTags(): LiveData<List<Tag>> = bookmarksRepository.getTags()

    fun getTagsSync(): List<Tag> = bookmarksRepository.getTagsSync()

    fun getTagsOfBookmark(url: String) = bookmarksRepository.getTagsOfBookmark(url)

    fun getTagsOfBookmarkSync(url: String) = bookmarksRepository.getTagsOfBookmarkSync(url)


    fun getBookmarksOfTagSync(tag: String) = bookmarksRepository.getBookmarksOfTagSync(tag)
}
