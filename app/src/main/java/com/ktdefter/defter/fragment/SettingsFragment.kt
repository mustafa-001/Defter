package com.ktdefter.defter.fragment

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    val bookmarksViewModel: BookmarksViewModel by viewModels()

    val getDocumentFileToExport = registerForActivityResult(ActivityResultContracts.CreateDocument()) { it ->
        Log.d("defter", "on ActivityResultCallback, data: $it")

        val contentResolver = requireActivity()!!.applicationContext.contentResolver
        val outputStream = contentResolver.openOutputStream(it!!)
        val be = JSONExporter(outputStream!!)
        val bookmarksViewModel: BookmarksViewModel by viewModels()
        be.export(bookmarksViewModel.getBookmarksSync())// Perform operations on the document using its URI.
    }
    val getDocumentFileToImport = registerForActivityResult(ActivityResultContracts.OpenDocument()) { it ->
        Log.d("defter", "on ActivityResultCallback, data: $it")

        val contentResolver = requireActivity().applicationContext.contentResolver
        val inputStream = contentResolver.openInputStream(it!!)
        val importedBookmarks = JSONImporter(inputStream!!)
        val bookmarksViewModel: BookmarksViewModel by viewModels()
        for (b in importedBookmarks.import()){
            bookmarksViewModel.addBookmark(b.url)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(root_preferences, rootKey)
        val prefs = findPreference<Preference>("export")
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {

        //Without this getDocumentFile.ActivityResultCallback requires bookmarksViewModel for
        // the first time. Activity is not fully restored/constructed when this callback is called.
        // This causes IllegalStateException to be thrown.
        val dummy_variable = bookmarksViewModel.getBookmarksSync()
        when (preference.key) {
            "export" -> {
                Log.d("defter", "Clicked export preference")
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())!!
                Log.d("defter", "Export preference is clicked.")
                getDocumentFileToExport.launch("${date}_exported_defter.json")

            }
            "import" -> {
                getDocumentFileToImport.launch(arrayOf("application/json"))
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


class JSONExporter(
    private val outputStream: OutputStream
) : BookmarkExportable {
    override fun export(bookmarks: List<Bookmark>) {
        val encodedBookmarks = Json.encodeToString(bookmarks)
        Log.d("defter", "encoded bookmarks: $encodedBookmarks")
        outputStream.write(encodedBookmarks.toByteArray())
        outputStream.flush()
    }
}

class JSONImporter(
    private val inputStream: InputStream
) : BookmarkImportable {

    override fun import(): List<Bookmark> {
        return Json.decodeFromString<List<Bookmark>>(inputStream.readBytes().decodeToString())
    }
}

