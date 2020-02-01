package com.ktdefter.defter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksDatabase
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import com.ktdefter.defter.viewmodels.BookmarksViewModelFactory
import java.lang.IllegalStateException
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_select_tag_dialog.*
import kotlinx.android.synthetic.main.fragment_select_tag_dialog.view.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SelectTagDialogFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SelectTagDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectTagDialogFragment : DialogFragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var bookmarksViewModel: BookmarksViewModel
    lateinit var selectedBookmark: Bookmark
    lateinit var tags: List<Tag>
    lateinit var allTags: List<Tag>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return activity?.let {

            val selectedTags = allTags.map { tags.contains(it) }
            Log.d("Defter", "all tags are: ${allTags.map { it.tagName }}")
            val changes: MutableMap<String, Boolean> = mutableMapOf()
            val view = it.layoutInflater.inflate(R.layout.fragment_select_tag_dialog, null)
            view.apply {
                new_tag_text.setAdapter(
                    ArrayAdapter(context,
                        android.R.layout.simple_dropdown_item_1line,
                        allTags.map { it.tagName })
                )
                new_tag_text.threshold = 1
            }

            AlertDialog.Builder(it)
                .setView(view)

                .setTitle("Select new tag")

                .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }

                .setPositiveButton("Add",
                    DialogInterface.OnClickListener { _, _ ->
                        changes.mapKeys {
                            if (it.value == true) {
                                bookmarksViewModel.addBookmarkTagPair(selectedBookmark.url, it.key)
                            } else {
                            bookmarksViewModel.deleteBookmarkTagPair(selectedBookmark.url, it.key)
                            }
                        }
                        onPositiveClick(view.new_tag_text.text.toString())
                    })
                .create()
        } ?: throw IllegalStateException("Main Activity cannot be null")
    }

    private fun onPositiveClick(tag: String) {
        if (tag != "") {
            bookmarksViewModel.addBookmarkTagPair(selectedBookmark.url, tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO Make sure this way of getting activity's ViewModel is safe and proper way.
        // Commented part is old default ViewModelFactory method.
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(requireContext()).bookmarkDao(),
            BookmarksDatabase.getInstance(requireContext()).tagDao(),
            BookmarksDatabase.getInstance(requireContext()).bookmarkTagPairDao()
        )
        bookmarksViewModel = BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)
        allTags = bookmarksViewModel.getTagsSync()
        tags = bookmarksViewModel.getTagsOfBookmarkSync(selectedBookmark.url)
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
         * @return A new instance of fragment SelectTagDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            SelectTagDialogFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
