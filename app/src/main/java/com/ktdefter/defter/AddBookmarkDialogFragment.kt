package com.ktdefter.defter

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.ktdefter.defter.data.BookmarksDatabase
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import com.ktdefter.defter.viewmodels.BookmarksViewModelFactory
import java.lang.IllegalStateException
import kotlinx.android.synthetic.main.fragment_add_bookmark_dialog.*
import kotlinx.android.synthetic.main.fragment_add_bookmark_dialog.view.*
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddBookmarkDialogFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AddBookmarkDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddBookmarkDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters.
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var bookmarksViewModel: BookmarksViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).let {
                val view = requireActivity()
                    .layoutInflater
                    .inflate(R.layout.fragment_add_bookmark_dialog, null)

                it.setView(view)
                it.setTitle("Add new bookmark")
                it.setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { _, _ ->
                        getDialog()?.cancel()
                    })
                it.setPositiveButton("Add",
                    DialogInterface.OnClickListener { _, _ ->
                        onPositiveClick(view.findViewById<EditText>(R.id.url_text)?.text.toString())
                    })
                view.paste_clipboard_button.setOnClickListener {
                    val clipboard = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    if (clipboard.hasPrimaryClip()) {
                        view.url_text.append(clipboard.primaryClip!!.getItemAt(0).text)
                    }
                }
                it.create()
            }
            .apply {
                this.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        } ?: throw IllegalStateException("Main Activity cannot be null")
    }

    private fun onPositiveClick(url: String) {
        Log.d("Adding the url: ", url)
        bookmarksViewModel.addBookmark(url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO Make sure this way of getting activity's ViewModel is safe and proper way.
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(requireContext()).bookmarkDao(),
            BookmarksDatabase.getInstance(requireContext()).tagDao(),
            BookmarksDatabase.getInstance(requireContext()).bookmarkTagPairDao(),
            requireContext()
        )
        bookmarksViewModel = BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)
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


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddBookmarkDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddBookmarkDialogFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
