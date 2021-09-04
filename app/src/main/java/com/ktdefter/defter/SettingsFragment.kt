package com.ktdefter.defter

import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import android.content.Context
import android.os.FileUtils
import android.util.Log
import androidx.core.content.FileProvider
import androidx.preference.*
import androidx.fragment.*
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ktdefter.defter.R.xml.root_preferences
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksDatabase
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import com.ktdefter.defter.viewmodels.BookmarksViewModelFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess
import android.util.Log.d as d1

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var bookmarksViewModel: BookmarksViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(requireContext()).bookmarkDao(),
            BookmarksDatabase.getInstance(requireContext()).tagDao(),
            BookmarksDatabase.getInstance(requireContext()).bookmarkTagPairDao(),
            requireContext()
        )
        bookmarksViewModel =
            BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)

        setPreferencesFromResource(root_preferences, rootKey)
        val prefs = findPreference<Preference>("export")
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == "export") {
            Log.d("defter", "Clicked export preference")
            val be = BookmarksExporter(
                File(
                    requireContext().filesDir,
                    "exported_bookmarks.json"
                )
            )
            Log.d("defter", "Created BookmarksExporter class")
            be.export(bookmarksViewModel.getBookmarksSync())
        }
        return true
    }
}

class BookmarksExporter(
    val fd: File
) {
    fun export(bookmarks: List<Bookmark>) {
        val encodedBookmarks = Json.encodeToString(bookmarks)

        Log.d("defter", "encoded bookmarks: $encodedBookmarks")
        fd.writeText(encodedBookmarks)
        Log.d("defter", "writing to ${fd.absolutePath}")
    }

}

class BookmarkImporter {

}

