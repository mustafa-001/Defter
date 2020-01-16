package com.example.bookmarkmanager1

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.example.bookmarkmanager1.data.BookmarksDatabase
import com.example.bookmarkmanager1.data.BookmarksRepository
import com.example.bookmarkmanager1.viewmodels.BookmarksViewModel
import com.example.bookmarkmanager1.viewmodels.BookmarksViewModelFactory
import java.lang.Exception
import java.lang.IllegalStateException
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
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val view = inflater.inflate(R.layout.fragment_add_bookmark_dialog, null)

            builder.setView(view)
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener {_, _ ->
                        getDialog()?.cancel()
                    })
                .setPositiveButton("Add",
                    DialogInterface.OnClickListener {_, _ ->
                        onPositiveClick(view.findViewById<EditText>(R.id.url_text)?.text.toString())
                    })
            builder.create()
        } ?: throw IllegalStateException("Main Activity cannot be null")
    }


    private fun onPositiveClick(url: String){
        Log.d("url is: ", url)
        bookmarksViewModel.addBookmark(url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO Make sure this way of getting activity's ViewModel is safe and proper way.
        //Commented part is old default ViewModelFactory method.
        val bookmarksrepo = BookmarksRepository.getInstance(
            BookmarksDatabase.getInstance(requireContext()).bookmarkDao(),
            BookmarksDatabase.getInstance(requireContext()).tagDao(),
            BookmarksDatabase.getInstance(requireContext()).bookmarkTagPairDao()
        )
        bookmarksViewModel = BookmarksViewModelFactory(bookmarksrepo).create(BookmarksViewModel::class.java)
//        bookmarksViewModel = activity?.run{
//            (this)[BookmarksViewModel::class.java]
//        } ?: throw Exception("Activity doesn't have a viewmodel")
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
