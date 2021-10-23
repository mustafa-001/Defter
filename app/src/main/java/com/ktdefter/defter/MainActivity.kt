package com.ktdefter.defter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.fragment.AddBookmarkDialogFragment
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import com.ktdefter.defter.fragment.BookmarkListFragment
import com.ktdefter.defter.fragment.SelectTagDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AddBookmarkDialogFragment.OnFragmentInteractionListener,
    SelectTagDialogFragment.OnFragmentInteractionListener,
    BookmarkListFragment.OnFragmentInteractionListener {
    private val bookmarksViewModel: BookmarksViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView
    private lateinit var drawer: DrawerLayout
    private val oldTagIds: MutableList<Int> = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawer)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setDrawerTags()

//        supportFragmentManager.commit {
//            add<BookmarkListFragment>(R.id.nav_host_fragment)
//            setReorderingAllowed(true)
//            addToBackStack("nav_host")
//        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener(View.OnClickListener {
            val addBookmarkDialogFragment = AddBookmarkDialogFragment()
            addBookmarkDialogFragment.show(
                this.supportFragmentManager,
                "add_bookmark_fragment"
            )
        })

        bookmarksViewModel.searchKeyword = MutableLiveData(Optional.of("youtube"))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                Log.d("Defter", "intent: ${intent.getStringExtra(Intent.EXTRA_TEXT)}")
                bookmarksViewModel.addBookmark(intent.getStringExtra((Intent.EXTRA_TEXT))!!)
            }
        }
    }

    private fun setDrawerTags() {
        bookmarksViewModel.getTags().observe(this, { newTags ->
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
                Toast.makeText(this.applicationContext, "settings selected", Toast.LENGTH_SHORT)
                    .show()
                findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
