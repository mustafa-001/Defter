package com.ktdefter.defter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
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
class BookmarkListFragment() : Fragment(), DrawerLayout.DrawerListener {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var bookmarksView: RecyclerView
    private lateinit var bookmarksViewModel: BookmarksViewModel
    private val tagToShow = arguments?.getString("selectedTag")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is a workaround to not use sunflower InjectorUtils methoo.
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(requireContext()).bookmarkDao(),
            BookmarksDatabase.getInstance(requireContext()).tagDao(),
            BookmarksDatabase.getInstance(requireContext()).bookmarkTagPairDao()
        )
//
        bookmarksViewModel = BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)
//        val bookmarksViewModel = ViewModelProviders.of(this)[BookmarksViewModel::class.java]
    }

    override fun onDrawerClosed(drawerView: View) {
        Toast.makeText(drawerView.context, "Drawer Opened" , Toast.LENGTH_SHORT).show()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        Toast.makeText(drawerView.context, "Drawer Opened" , Toast.LENGTH_SHORT).show()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDrawerStateChanged(newState: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun onDrawerOpened(drawerView: View) {
        Toast.makeText(drawerView.context, "Drawer Opened" , Toast.LENGTH_SHORT).show()
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
                var bookmarks: LiveData<List<Bookmark>>?
                if (tagToShow == null) {
                    bookmarks = bookmarksViewModel.getBookmarks()
                } else {
                    bookmarks = bookmarksViewModel.getBookmarksOfTag(tagToShow)
                }
                bookmarks.observe(this@BookmarkListFragment, Observer<List<Bookmark>> { newBookmarks ->
                    bookmarkAdapter.bookmarks = newBookmarks
                    bookmarkAdapter.notifyDataSetChanged()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookmarkListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookmarkListFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
