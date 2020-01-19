package com.ktdefter.defter.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksRepository

class BookmarksViewModel internal constructor(private val bookmarksRepository: BookmarksRepository): ViewModel(){
   private var position = 0

    val bookmarks: LiveData<List<Bookmark>> = bookmarksRepository.getBookmarks()

//    fun

   fun addBookmark(url: String) {
       return bookmarksRepository.insertBookmark(url)
   }

    fun deleteBookmark(url: String){
        bookmarksRepository.deleteBookmark(url)
    }

}
