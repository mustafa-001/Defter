package com.ktdefter.defter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksDatabase
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import com.ktdefter.defter.viewmodels.BookmarksViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import layout.BookmarkAdapter

class MainActivity : AppCompatActivity(), AddBookmarkDialogFragment.OnFragmentInteractionListener,
SelectTagDialogFragment.OnFragmentInteractionListener, BookmarkListFragment.OnFragmentInteractionListener{
    private lateinit var bookmarksViewModel: BookmarksViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)

        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        navView.menu.add("Deneme")
        navView.menu.add("Deneme2")
        navView.menu.add("Deneme2")

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawer)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // This is a workaround to not use sunflower InjectorUtils methoo.
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(applicationContext).bookmarkDao(),
            BookmarksDatabase.getInstance(applicationContext).tagDao(),
            BookmarksDatabase.getInstance(applicationContext).bookmarkTagPairDao()
        )

        bookmarksViewModel =
            BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)


        fab.setOnClickListener(View.OnClickListener {
            val addBookmarkDialogFragment = AddBookmarkDialogFragment()
            addBookmarkDialogFragment.show(
                this.supportFragmentManager,
                "add_bookmark_fragment"
            )
        })

        when {
            getIntent().action == Intent.ACTION_SEND -> {
                bookmarksViewModel.addBookmark(intent.getStringExtra((Intent.EXTRA_TEXT)))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // TODO why without this AddBookmarkDialogFragment doesn't work.
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
