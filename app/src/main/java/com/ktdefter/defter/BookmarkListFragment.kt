package com.ktdefter.defter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksDatabase
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import com.ktdefter.defter.viewmodels.BookmarksViewModelFactory
import kotlinx.android.synthetic.main.fragment_bookmark_list.*
import layout.BookmarkAdapter

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BookmarkListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BookmarkListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookmarkListFragment() : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var bookmarksView: RecyclerView
    private lateinit var bookmarksViewModel: BookmarksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is a workaround to not use sunflower InjectorUtils methoo.
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(requireContext()).bookmarkDao(),
            BookmarksDatabase.getInstance(requireContext()).tagDao(),
            BookmarksDatabase.getInstance(requireContext()).bookmarkTagPairDao(),
            requireContext()
        )
        bookmarksViewModel = BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)

        //Overrride back button and direclty return to homelist instead of returning to another bookmarks of tag list.
        if (findNavController().currentDestination != findNavController().graph.findNode(R.id.nav_home)) {
                requireActivity().onBackPressedDispatcher.addCallback(this) {
                    while (findNavController().currentDestination != findNavController().graph.findNode(R.id.nav_home)) {
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
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_bookmark_list, container, false)
            .apply {
                val viewManager = LinearLayoutManager(requireContext())
                val bookmarkAdapter = BookmarkAdapter()
                bookmarkAdapter.viewModel = bookmarksViewModel
                bookmarksView = this.findViewById<RecyclerView>(R.id.bookmarks_recycler_view).apply {
                    layoutManager = viewManager
                    adapter = bookmarkAdapter
                }

                // TODO Dont use notifyDataSetChanged(), use diffutils or something.
                // TODO is observing whole list is good or can we do better?

                val tagToShow = arguments?.getString("selectedTag")
                bookmarksViewModel.getBookmarksOfTag(tagToShow).observe(this@BookmarkListFragment, Observer<List<Bookmark>> { newBookmarks ->
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
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
