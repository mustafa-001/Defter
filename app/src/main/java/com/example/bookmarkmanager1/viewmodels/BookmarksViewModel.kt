package com.example.bookmarkmanager1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookmarkmanager1.data.Bookmark
import com.example.bookmarkmanager1.data.BookmarksRepository

class BookmarksViewModel internal constructor(bookmarksrepo: BookmarksRepository): ViewModel(){
   private var position = 0

    val bookmarksrepo = bookmarksrepo

    val bookmarks: LiveData<List<Bookmark>> = bookmarksrepo.getBookmarks()

   fun addBookmark(url: String) {
       return bookmarksrepo.insertBookmark(url)
   }

    fun deleteBookmark(url: String){
        bookmarksrepo.deleteBookmarm(url)
    }

   private fun loadBookmarks(){
      //fetch users from database.
   }
}
