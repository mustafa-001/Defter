package com.ktdefter.defter.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_bookmark_dialog.view.*
import timber.log.Timber

@AndroidEntryPoint
class AddBookmarkDialogFragment : DialogFragment() {
    private var listener: OnFragmentInteractionListener? = null
    val viewModel: BookmarksViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).run {
                val view = requireActivity()
                    .layoutInflater
                    .inflate(R.layout.fragment_add_bookmark_dialog,
                        view?.findViewById(R.id.bookmarks_list_fragment)
                    )

                setView(view)
                setTitle("Add new bookmark")
                setNegativeButton("Cancel"
                ) { _, _ ->
                    dialog?.cancel()
                }
                setPositiveButton("Add"
                ) { _, _ ->
                    onPositiveClick(view.findViewById<EditText>(R.id.url_text)?.text.toString())
                }
                view.paste_clipboard_button.setOnClickListener {
                    val clipboard = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    if (clipboard.hasPrimaryClip()) {
                        view.url_text.append(clipboard.primaryClip!!.getItemAt(0).text)
                    }
                }
                create()
            }
            .apply {
                this.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        } ?: throw IllegalStateException("Main Activity cannot be null")
    }

    private fun onPositiveClick(url: String) {
        Timber.d(url)
        viewModel.addBookmark(Bookmark(url))
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


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment AddBookmarkDialogFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
////            AddBookmarkDialogFragment().apply {
////                arguments = Bundle().apply {
////                }
//            }
//    }
}
