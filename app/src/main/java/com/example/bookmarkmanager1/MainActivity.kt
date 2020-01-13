package com.example.bookmarkmanager1

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmarkmanager1.data.Bookmark
import com.example.bookmarkmanager1.data.BookmarksDatabase
import com.example.bookmarkmanager1.data.BookmarksRepository
import com.example.bookmarkmanager1.viewmodels.BookmarksViewModelFactory
import com.example.bookmarkmanager1.viewmodels.BookmarksViewModel

import kotlinx.android.synthetic.main.activity_main.*
import layout.BookmarkAdapter

class MainActivity : AppCompatActivity(), AddBookmarkDialogFragment.OnFragmentInteractionListener, BookmarkAdapter.OnBookmarkContextMenuListener {
    private lateinit var bookmarksView: RecyclerView
    private lateinit var bookmarksViewModel: BookmarksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO implement action bar cleanly or remove it.
        setSupportActionBar(toolbar)

        //This is a workaround to not use sunflower InjectorUtils methoo.
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(applicationContext).bookmarksDao()
        )
        bookmarksViewModel =
            BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)
//        val bookmarksViewModel = ViewModelProviders.of(this)[BookmarksViewModel::class.java]


        val viewManager = LinearLayoutManager(this)
        val bookmarkAdapter = BookmarkAdapter()

        bookmarksView = findViewById<RecyclerView>(R.id.bookmarks_recycler_view).apply {
            layoutManager = viewManager
            adapter = bookmarkAdapter
        }
        bookmarkAdapter.setOnBookmarkContextMenuListener(this)

        //when vm.bookmarks changes, ViewModel(in future database) calls this function
        //TODO Dont use notifyDataSetChanged(), use diffutils or something.
        //TODO is observing whole list is good or can we do better?
        bookmarksViewModel.bookmarks.observe(this, Observer<List<Bookmark>> { newBookmarks ->
            bookmarkAdapter.bookmarks = newBookmarks
            bookmarkAdapter.notifyDataSetChanged()
        })

        fab.setOnClickListener(View.OnClickListener {
            val addBookmarkDialogFragment = AddBookmarkDialogFragment()
            addBookmarkDialogFragment.show(
                this.supportFragmentManager,
                "add_bookmark_dialog_fragment"
            )
        })

        intent = getIntent()
        when{
            intent.action == Intent.ACTION_SEND -> {
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

    //TODO why without this AddBookmarkDialogFragment doesn't work.
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBookmarkDeleteClicked(url: String) {
        bookmarksViewModel.deleteBookmark(url)
    }

    //TODO implement onBookmarkChangeTagsCLicked
    override fun onBookmarkClicked(url: String) {
        val openLinkIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
        }
        startActivity(openLinkIntent)
    }

    override fun onBookmarkChangeTagsClicked(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
