package com.ktdefter.defter.fragment

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.ktdefter.defter.R.xml.root_preferences
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    val bookmarksViewModel: BookmarksViewModel by viewModels()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(root_preferences, rootKey)
        val prefs = findPreference<Preference>("export")
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {

        when (preference.key) {
            "export" -> {
                Log.d("defter", "Clicked export preference")
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())!!
                val be = JSONExporterImporter(
                    File(
                        requireContext().filesDir,
                        "$date _exported_bookmarks_defter.json"
                    )
                )
                be.export(bookmarksViewModel.getBookmarksSync())
            }
            "import" -> {
                if (preference.key == "import") {
                    SelectImportFileFragment().show((activity as AppCompatActivity).supportFragmentManager, "select_import_file")
                    val be = JSONExporterImporter(
                        File(
                            requireContext().filesDir,
                            "exported_bookmarks.json"
                        )
                    )
//                    be.import().forEach { b ->
//                        bookmarksViewModel.addBookmark(b.url)
//                        b.tags.forEach { t ->
//                            bookmarksViewModel.addBookmarkTagPair(b.url, t.tagName)
//                        }
//                    }

                }
            }
        }
        return true
    }
}

    interface BookmarkExportable {
        fun export(bookmarks: List<Bookmark>)
    }
    interface BookmarkImportable {
        fun import(): List<Bookmark>
    }

    class JSONExporterImporter(
        private val fd: File
    ) : BookmarkExportable, BookmarkImportable {
        override fun export(bookmarks: List<Bookmark>) {
            val encodedBookmarks = Json.encodeToString(bookmarks)
            Log.d("defter", "encoded bookmarks: $encodedBookmarks")
            fd.writeText(encodedBookmarks)
            Log.d("defter", "writing to ${fd.absolutePath}")
        }

        override fun import(): List<Bookmark> {
            Log.d("defter", "decoding bookmarks from ${fd.absolutePath}")
            return Json.decodeFromString<List<Bookmark>>(fd.readText(Charsets.UTF_8))
        }
    }

