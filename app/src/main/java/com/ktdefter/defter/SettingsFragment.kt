package com.ktdefter.defter

import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import android.content.Context
import androidx.preference.*
import com.ktdefter.defter.R.xml.root_preferences
import com.ktdefter.defter.data.BookmarksDatabase
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.viewmodels.BookmarksViewModelFactory
import kotlin.system.exitProcess

class SettingsFragment : PreferenceFragmentCompat(){

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(root_preferences, rootKey)
        val prefs = findPreference<Preference>("export")
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == "export") {
            Toast.makeText(
                preference.context,
                "Export function is not implemented yet!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return true
    }
}

class BookmarksExporter{
//
//    val bookmarksrepo = BookmarksRepository.getInstance(
//        BookmarksDatabase.getInstance(requireContext()).bookmarkDao(),
//        BookmarksDatabase.getInstance(requireContext()).tagDao(),
//        BookmarksDatabase.getInstance(requireContext()).bookmarkTagPairDao(),
//        requireContext()
//    )
//    val bookmarksViewModel = BookmarksViewModelFactory(bookmarksrepo).create
//    (BookmarksViewModel::class.java)

}

class BookmarkImporter{

}

