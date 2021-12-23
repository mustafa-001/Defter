package com.ktdefter.defter.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.R
import com.ktdefter.defter.data.SortBy
import com.ktdefter.defter.data.SortDirection
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.fragment.adapter.BookmarkAdapter
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.app_bar_main.*
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class BookmarkListFragment : Fragment(), SearchView.OnQueryTextListener,
    MenuItem.OnMenuItemClickListener {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var bookmarksView: RecyclerView
    private lateinit var tag: Optional<Tag>
    val bookmarksViewModel: BookmarksViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Override back button and directly return to home list instead of returning to another bookmarks of tag list.
        tag = if (arguments?.getString("selectedTag") == null) {
            Optional.empty<Tag>()
        } else {
            Optional.of(Tag(arguments?.getString("selectedTag")!!))
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (bookmarksViewModel.bookmarksToShow.value?.any { it.isSelected }!!) {
                bookmarksViewModel.bookmarksToShow.postValue(bookmarksViewModel.bookmarksToShow.value?.map { b ->
                    if (b.isSelected) {
                        return@map b.copy().apply { this.isSelected = false }
                    } else b
                })
                disableMultipleSelection()
                return@addCallback

            }
            if (findNavController().currentDestination?.id != findNavController().graph.startDestination) {
                Timber.d("onBackPressedCallback when currentDestination is not startDestination")

                findNavController().navigate(R.id.nav_home)
//                while (findNavController().currentDestination != findNavController().graph.findNode(
//                        R.id.nav_home
//                    )
//                ) {
//                    findNavController().navigateUp()
//                }
            } else {
                requireActivity().finish()
//                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    fun activateMultipleSelection() {
        val act = requireActivity() as AppCompatActivity
        act.supportActionBar?.invalidateOptionsMenu()
        Timber.d("onEnableMultipleSelection")
    }

    @SuppressLint("RestrictedApi")
    fun disableMultipleSelection() {
        val act = requireActivity() as AppCompatActivity
        act.supportActionBar?.invalidateOptionsMenu()
        Timber.d("onDisableMultipleSelection")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        bookmarksViewModel.tag = tag
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_bookmark_list, container, false)
            .apply {
                val bookmarksListLayoutManager = LinearLayoutManager(requireContext())
                val bookmarksListAdapter = BookmarkAdapter(
                    bookmarksViewModel
                )
                bookmarksView =
                    this.findViewById<RecyclerView>(R.id.bookmarks_recycler_view).apply {
                        layoutManager = bookmarksListLayoutManager
                        adapter = bookmarksListAdapter
                    }

                // TODO Don't use notifyDataSetChanged(), use diffutils or something.
                // TODO is observing whole list is good or can we do better?
                bookmarksViewModel.bookmarksToShow.observe(viewLifecycleOwner, { newBookmarks ->
                    bookmarksListAdapter.setBookmarks(newBookmarks)
                })
                return this
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sortDirection) {
            if (item.isChecked) {
                bookmarksViewModel.sortDirection = SortDirection.ASC
            } else {
                bookmarksViewModel.sortDirection = SortDirection.DESC
            }
            item.isChecked = item.isChecked.not()
            return true
        } else if (item.itemId == R.id.sortByName) {
            if (item.isChecked) {
                bookmarksViewModel.sortBy = SortBy.MODIFICATION_TIME
            } else {
                bookmarksViewModel.sortBy = SortBy.HOSTNAME
            }
            item.isChecked = item.isChecked.not()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(this)
        menu.findItem(R.id.action_delete).setOnMenuItemClickListener(this)
    }

    private fun onMultipleItemDelete() {
        bookmarksViewModel.bookmarksToShow.value
            ?.filter { it.isSelected }
            ?.forEach { bookmarksViewModel.deleteBookmark(it.url) }
        disableMultipleSelection()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (
            bookmarksViewModel.bookmarksToShow.value?.filter { it.isSelected }?.any() == true) {
            menu.forEach {
                it.isVisible = it.groupId == R.id.multiple_selection
            }
        } else {
            menu.forEach {
                it.isVisible = it.groupId != R.id.multiple_selection

            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val newQuery: Optional<String> = if (query == null) {
            Optional.empty<String>()
        } else Optional.of(query)
        bookmarksViewModel.searchKeyword = newQuery
        Timber.d("SearchView.onTextSubmit() with query: $newQuery")
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        val newQuery: Optional<String> = if (query == null) {
            Optional.empty<String>()
        } else Optional.of(query)
        bookmarksViewModel.searchKeyword = newQuery
        Timber.d("SearchView.onTextSubmit() with query: $newQuery")
        return true
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        if (p0?.itemId == R.id.action_delete) {
            onMultipleItemDelete()
        }
        return true
    }

}
