package com.ktdefter.defter.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import kotlinx.android.synthetic.main.fragment_bookmark_list.*
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.fragment.adapter.BookmarkAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BookmarkListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BookmarkListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class BookmarkListFragment() : Fragment(),  SearchView.OnQueryTextListener {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var bookmarksView: RecyclerView
    private lateinit var tag: Optional<Tag>
    val  bookmarksViewModel: BookmarksViewModel by activityViewModels<BookmarksViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Overrride back button and direclty return to homelist instead of returning to another bookmarks of tag list.
        tag = if (arguments?.getString("selectedTag") ==null) {
            Optional.empty<Tag>()
        } else{
            Optional.of(arguments?.getString("selectedTag")!!.let { Tag(it) })
        }
        if (findNavController().currentDestination != findNavController().graph.findNode(R.id.nav_home)) {
                requireActivity().onBackPressedDispatcher.addCallback(this) {
                    while (findNavController().currentDestination != findNavController().graph.findNode(
                            R.id.nav_home
                        )) {
                        findNavController().navigateUp()
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        bookmarksViewModel.tag.postValue(tag)
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_bookmark_list, container, false)
            .apply {
                val viewManager = LinearLayoutManager(requireContext())
                val bookmarkAdapter = BookmarkAdapter(requireActivity().supportFragmentManager)
                bookmarkAdapter.viewModel = bookmarksViewModel
                bookmarksView = this.findViewById<RecyclerView>(R.id.bookmarks_recycler_view).apply {
                    layoutManager = viewManager
                    adapter = bookmarkAdapter
                }

                // TODO Dont use notifyDataSetChanged(), use diffutils or something.
                // TODO is observing whole list is good or can we do better?

                bookmarksViewModel.bookmarksToShow.observe(viewLifecycleOwner, { newBookmarks ->
                    bookmarkAdapter.bookmarks = newBookmarks
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val newQuery: Optional<String> = if (query == null){
            Optional.empty<String>()
        } else Optional.of(query)
        bookmarksViewModel.searchKeyword.postValue(newQuery)
        Log.d("defter", "SearchView.onTextSubmit() with query: $newQuery")
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        val newQuery: Optional<String> = if (query == null){
            Optional.empty<String>()
        } else Optional.of(query)
        bookmarksViewModel.searchKeyword.postValue(newQuery)
        Log.d("defter", "SearchView.onTextSubmit() with query: $newQuery")
        return true
    }

    /**
     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
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
