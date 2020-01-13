package com.example.bookmarkmanager1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.bookmarkmanager1.data.Bookmark
import com.example.bookmarkmanager1.data.BookmarksRepository

class BookmarksViewModel internal constructor(bookmarksRepository: BookmarksRepository): ViewModel(){
   private var position = 0

    private val bookmarksRepository = bookmarksRepository

    val bookmarks: LiveData<List<Bookmark>> = bookmarksRepository.getBookmarks()

//    fun

   fun addBookmark(url: String) {
       return bookmarksRepository.insertBookmark(url)
   }

    fun deleteBookmark(url: String){
        bookmarksRepository.deleteBookmarm(url)
    }

   private fun loadBookmarks(){
      //fetch users from database.
   }
}
