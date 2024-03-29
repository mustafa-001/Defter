package com.ktdefter.defter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.fragment.AddBookmarkDialogFragment
import com.ktdefter.defter.fragment.BookmarkListFragment
import com.ktdefter.defter.fragment.SelectTagDialogFragment
import com.ktdefter.defter.fragment.SettingsFragment
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AddBookmarkDialogFragment.OnFragmentInteractionListener,
    SelectTagDialogFragment.OnFragmentInteractionListener,
    BookmarkListFragment.OnFragmentInteractionListener, LifecycleOwner {
    val bookmarksViewModel: BookmarksViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView
    private lateinit var drawer: DrawerLayout
    private val oldTagIds: MutableList<Int> = mutableListOf()
    private lateinit var tags: LiveData<List<Tag>>
    private lateinit var notificationManager: NotificationManager
    val CHANNEL_ID = "Download URL Data"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("useDarkTheme", false)) {
            setTheme(R.style.AppTheme)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        } else {
            setTheme(R.style.AppTheme)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
        Timber.plant(Timber.DebugTree())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawer)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        notificationManager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            notificationManager.createNotificationChannel(channel)
        }


        tags = bookmarksViewModel.getTags()
        setDrawerTags()

        if (getSharedPreferences("SyncSettings", 0).getBoolean(
                "syncOnStart", false
            )
        ) {
            bookmarksViewModel.bookmarksRepository.syncBookmarks()
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val addBookmarkDialogFragment = AddBookmarkDialogFragment()
            addBookmarkDialogFragment.show(
                this.supportFragmentManager,
                "add_bookmark_fragment"
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                Timber.d("on ACTION_SEND intent : " + intent.getStringExtra(Intent.EXTRA_TEXT))
                bookmarksViewModel.addBookmark(Bookmark(intent.getStringExtra((Intent.EXTRA_TEXT))!!))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(status: BookmarksViewModel.DownloadStatus) {
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setOngoing(status.failed+status.successful != status.max)
            .setSmallIcon(R.drawable.ic_baseline_open_in_browser_24)
            .setContentTitle("Download bookmark info")
            .setContentText("Downloading info for ${status.successful}  from ${status.max} bookmark, ${status.failed} failed ")
            .setProgress(status.max, status.failed + status.successful, false)
            .build()
        notificationManager.notify(100, notification)
    }

    private fun setDrawerTags() {
        navView.menu.findItem(R.id.drawer_settings).setOnMenuItemClickListener {
            drawer.closeDrawer(GravityCompat.START, true)
            findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment)
            true
        }
        this.tags.observe(this, { newTags ->
            oldTagIds.map {
                navView.menu.removeItem(it)
            }
            oldTagIds.clear()

            for (newTag in newTags) {
                val menuItem = navView.menu.add(
                    R.id.tags_drawer,
                    newTags.indexOf(newTag),
                    newTags.indexOf(newTag),
                    newTag.tagName
                )
                oldTagIds.add(menuItem.itemId)

                menuItem.setOnMenuItemClickListener {
                    val bundle = Bundle()
                    bundle.putString("selectedTag", menuItem.title.toString())
                    navController.navigate(R.id.nav_show_bookmarks_of_tag, bundle)
                    drawer.closeDrawer(GravityCompat.START, true)
                    true
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment)
                true
            }
            R.id.action_search -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
