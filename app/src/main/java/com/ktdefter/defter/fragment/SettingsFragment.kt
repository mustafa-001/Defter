package com.ktdefter.defter.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.ktdefter.defter.R.xml.root_preferences
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    val bookmarksViewModel: BookmarksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().fab.hide()
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().fab.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.forEach { it.setEnabled(false) }
    }

    private val getDocumentFileToExport =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { it ->
            Timber.d("on ActivityResultCallback, data: $it")

            val contentResolver = requireActivity().applicationContext.contentResolver
            val outputStream = contentResolver.openOutputStream(it!!)
            val be = JSONExporter(outputStream!!)
            val bookmarksViewModel: BookmarksViewModel by viewModels()
            be.export(bookmarksViewModel.getBookmarksSync())// Perform operations on the document using its URI.
        }
    private val getDocumentFileToImport =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { it ->
            Timber.d("on ActivityResultCallback, data: $it")

            val contentResolver = requireActivity().applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(it!!)
            //Cannot read extension when file is opened from Documents directory.
            val importedBookmarks: BookmarkImportable = when (it.path?.takeLast(4)) {
                "html" -> HTMLImporter(inputStream!!)
                "json" -> JSONImporter(inputStream!!)
                else -> throw Exception("Unreachable")
            }
            val bookmarksViewModel: BookmarksViewModel by viewModels()
            for (b in importedBookmarks.import()) {
                bookmarksViewModel.addBookmark(b)
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
                Timber.d("Clicked export preference")
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
                Timber.d("Export preference is clicked.")
                getDocumentFileToExport.launch("${date}_exported_defter.json")

            }
            "import" -> {
                getDocumentFileToImport.launch(arrayOf("text/html", "application/json"))
            }
            "useDarkTheme" -> {
                val p = preference as SwitchPreferenceCompat
                if (p.isChecked) {
                    Timber.d("dark theme selected")

                }
                val intent: Intent =
                    requireActivity().baseContext.packageManager.getLaunchIntentForPackage(
                        requireActivity().baseContext.packageName
                    )!!
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
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

class HTMLImporter(
    private val inputStream: InputStream
) : BookmarkImportable {
    override fun import(): List<Bookmark> {
        val doc = Jsoup.parse(inputStream, "UTF-8", "")
        var bookmarks = mutableListOf<Bookmark>()
        val links = doc.select("a[href]")
        links.forEach { Timber.d("""adding ${it.attr("href")}to bookmarks""") }
        return links
            .map { Bookmark(it.attr("href").toString()) }
    }
}

class JSONExporter(
    private val outputStream: OutputStream
) : BookmarkExportable {
    override fun export(bookmarks: List<Bookmark>) {
        val encodedBookmarks = Json.encodeToString(bookmarks)
        Timber.d("encoded bookmarks: $encodedBookmarks")
        outputStream.write(encodedBookmarks.toByteArray())
        outputStream.flush()
    }
}

class JSONImporter(
    private val inputStream: InputStream
) : BookmarkImportable {

    override fun import(): List<Bookmark> {
        return Json.decodeFromString(inputStream.readBytes().decodeToString())
    }
}

