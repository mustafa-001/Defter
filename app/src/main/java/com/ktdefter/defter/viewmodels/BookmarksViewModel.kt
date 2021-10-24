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
    var tag: Optional<Tag> = Optional.empty()

    var searchKeyword: MutableLiveData<Optional<String>> =
        MutableLiveData(Optional.of("google"))
    set(value) {
        field = value
        Log.d("defter", "seach keyword is reassigned")
    }
    val bookmarksToShow: MediatorLiveData<List<Bookmark>> = getBookmarks()

    // TODO Add tags witch switchmap to bookmarks, dont observer Livedata<List<Tag>>
    // from Adapter
    private fun getBookmarks(): MediatorLiveData<List<Bookmark>> {

       val r = MediatorLiveData<List<Bookmark>>()
           r.addSource(searchKeyword, {
            if (it.isPresent){
                Log.d("defter", "filtering")
                r.addSource(bookmarksRepository.getBookmarks()) {bookmarks ->
                    r.value = bookmarks.filter { it.url.contains(searchKeyword.value!!.get()) }
                }
            } else {
                r.addSource(bookmarksRepository.getBookmarks()) {
                    r.value = it
                }
                Log.d("defter", "not filtering")
            }
        })
        return r
//        val bookmarksToObserve: MediatorLiveData<List<Bookmark>> =
//            Transformations.switchMap(searchKeyword) {
//
//                val filteredWithTag = if (tag.isPresent) {
//                    bookmarksRepository.getBookmarksOfTag(tag.get().tagName)
//                } else {
//                    bookmarksRepository.getBookmarks()
//                }
//                if (searchKeyword.value!!.isPresent) {
//                    Log.d("defter", "filtering")
//                    Transformations.map(filteredWithTag) {
//                        it.filter { it.url.contains(searchKeyword.value!!.get()) }
//                    }
//                } else {
//                    Log.d("defter", "not filtering")
//                    filteredWithTag
//                }
//
//            }
//        bookmarksToShow = Transformations.map(bookmarksToObserve) { bookmarks ->
//            bookmarks.map { it.tags = bookmarksRepository.getTagsOfBookmarkSync(it.url) }
//            bookmarks
//        }
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
