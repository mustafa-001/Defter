package com.ktdefter.defter.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
class BookmarkListFragment() : Fragment(), SearchView.OnQueryTextListener {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var bookmarksView: RecyclerView
    private lateinit var tag: Optional<Tag>
    val bookmarksViewModel: BookmarksViewModel by activityViewModels<BookmarksViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Override back button and directly return to home list instead of returning to another bookmarks of tag list.
        tag = if (arguments?.getString("selectedTag") == null) {
            Optional.empty<Tag>()
        } else {
            Optional.of(arguments?.getString("selectedTag")!!.let { Tag(it) })
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (bookmarksViewModel.bookmarksToShow.value?.filter { it.isSelected }
                    ?.isNotEmpty()!!) {
               bookmarksViewModel.bookmarksToShow.postValue(bookmarksViewModel.bookmarksToShow.value?.map { b ->
                    if (b.isSelected == true) {
                        return@map b.copy().apply { this.isSelected = false }
                    } else b
                })
                disableMultipleSelection()
                return@addCallback
            }
            while (findNavController().currentDestination != findNavController().graph.findNode(
                    R.id.nav_home
                )
            ) {
                findNavController().navigateUp()
            }
        }
    }

    fun activateMultipleSelection() {
        val act = requireActivity() as AppCompatActivity
        act.supportActionBar!!.hide()
        Timber.d("onEnableMultipleSelection")
    }

    fun disableMultipleSelection() {
        val act = requireActivity() as AppCompatActivity
        act.supportActionBar!!.show()
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
                    requireActivity().supportFragmentManager,
                    bookmarksViewModel
                )
                bookmarksView =
                    this.findViewById<RecyclerView>(R.id.bookmarks_recycler_view).apply {
                        layoutManager = bookmarksListLayoutManager
                        adapter = bookmarksListAdapter
                    }

                // TODO Dont use notifyDataSetChanged(), use diffutils or something.
                // TODO is observing whole list is good or can we do better?
                bookmarksViewModel.bookmarksToShow.observe(viewLifecycleOwner, { newBookmarks ->
                    bookmarksListAdapter.setBookmarks(newBookmarks)
                })
                return this
            }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
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
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        val newQuery: Optional<String> = if (query == null) {
            Optional.empty<String>()
        } else Optional.of(query)
        bookmarksViewModel.searchKeyword = newQuery
        Log.d("defter", "SearchView.onTextSubmit() with query: $newQuery")
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        val newQuery: Optional<String> = if (query == null) {
            Optional.empty<String>()
        } else Optional.of(query)
        bookmarksViewModel.searchKeyword = newQuery
        Log.d("defter", "SearchView.onTextSubmit() with query: $newQuery")
        return true
    }

    /**
     * This interface must be implemented by activities that contain this
    //     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
