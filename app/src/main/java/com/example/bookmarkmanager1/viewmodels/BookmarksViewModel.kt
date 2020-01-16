package com.example.bookmarkmanager1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.bookmarkmanager1.data.Bookmark
import com.example.bookmarkmanager1.data.BookmarksRepository

class BookmarksViewModel internal constructor(private val bookmarksRepository: BookmarksRepository): ViewModel(){
   private var position = 0

    val bookmarks: LiveData<Array<Bookmark>> = bookmarksRepository.getBookmarks()

//    fun

   fun addBookmark(url: String) {
       return bookmarksRepository.insertBookmark(url)
   }

    fun deleteBookmark(url: String){
        bookmarksRepository.deleteBookmark(url)
    }

   private fun loadBookmarks(){
      //fetch users from database.
   }
}
