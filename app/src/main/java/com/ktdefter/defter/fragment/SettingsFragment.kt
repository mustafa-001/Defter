package com.ktdefter.defter.fragment

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ktdefter.defter.MainActivity
import com.ktdefter.defter.R
import com.ktdefter.defter.R.xml.root_preferences
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import com.ktdefter.defter.viewmodels.LoginViewModel
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
    val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().fab.hide()
        setHasOptionsMenu(true)
        bookmarksViewModel.downloadStatus.observe(this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (parentFragment!!.activity as MainActivity).showNotification(it)
            }
        }
        //     val notification = Notification.Builder(this, CHANNEL_ID)
        //         .setOngoing(true)
        //         .setSmallIcon(R.drawable.ic_baseline_open_in_browser_24)
        //         .setContentTitle("Progress")
        //         .setContentText("Progress details")
        //         .setProgress(it.maxDownloads, it.currentDownloads, false)
        //         .build()
        //
        //     requireActivity().getSystemService(NOTIF).notify(100, notification)
        // }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().fab.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.forEach { it.isEnabled = false }
    }

    private val getDocumentFileToExport =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) {
            Timber.d("on ActivityResultCallback, data: $it")
            Timber.d("Callback name is getDocumentFileToExport")
            val contentResolver = requireActivity().applicationContext.contentResolver
            val outputStream = contentResolver.openOutputStream(it!!)
            val be = JSONExporter(outputStream!!)
            val bookmarksViewModel: BookmarksViewModel by viewModels()
            be.export(bookmarksViewModel.getBookmarksSync())// Perform operations on the document using its URI.
        }
    private val getDocumentFileToImport =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            Timber.d("on ActivityResultCallback, data: $it")
            Timber.d("Callback name is getDocumentFileToImport")
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
        val syncSwitch = findPreference<Preference>("sync") as SwitchPreferenceCompat
        if (Firebase.auth.currentUser != null) {
            syncSwitch.isChecked = true
            syncSwitch.summaryOn = Firebase.auth.currentUser!!.email.toString()
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {

        //Without this getDocumentFile.ActivityResultCallback requires bookmarksViewModel for
        // the first time. Activity is not fully restored/constructed when this callback is called.
        // This causes IllegalStateException to be thrown.
        val dummyVariable = bookmarksViewModel.getBookmarksSync()
        when (preference.key) {
            "export" -> {
                val date =
                    SimpleDateFormat(
                        getString(R.string.export_file_datetime_format),
                        Locale.US
                    ).format(Date())
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
            "downloadMetadataForAll" -> {
                bookmarksViewModel.downloadMissingMetadataForAll()
            }
            "syncNow" -> {
                bookmarksViewModel.bookmarksRepository.syncBookmarks()
            }
            "syncOnStart" -> {
                val p = preference as SwitchPreferenceCompat
                requireContext().getSharedPreferences(
                    "SyncSettings", 0
                ).edit()
                    .putBoolean(
                        "syncOnStart",
                        p.isChecked
                    ).apply()
            }

            "sync" -> run {
                if ((preference as SwitchPreferenceCompat).isChecked) {
                    findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
                } else {
                    val loginViewModel: LoginViewModel by viewModels()
                    loginViewModel.logout()
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

class HTMLImporter(
    private val inputStream: InputStream
) : BookmarkImportable {
    override fun import(): List<Bookmark> {
        val doc = Jsoup.parse(inputStream, "UTF-8", "")
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

