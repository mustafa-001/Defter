package com.example.bookmarkmanager1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookmarkmanager1.data.BookmarksRepository

/**
 * Factory for creating a [BookmarksViewModel] with a constructor that takes a [BookmarksRepository].
 */
class BookmarksViewModelFactory(
    private val repository: BookmarksRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = BookmarksViewModel(repository) as T
}