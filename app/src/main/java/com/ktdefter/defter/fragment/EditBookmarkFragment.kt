package com.ktdefter.defter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.viewmodels.BookmarksViewModel

class EditBookmarkFragment : Fragment() {

    private val viewModel: BookmarksViewModel by activityViewModels<BookmarksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val url = requireArguments().getString("url")!!
        val tag = viewModel.getTagsOfBookmark(url)
        return inflater.inflate(R.layout.edit_bookmark_fragment, container, false).also { view ->
            val editText = view.findViewById<EditText>(R.id.editBookmarkFragment_url)
            editText.setText(url)
            view.findViewById<Button>(R.id.editBookmarkFragment_submit).setOnClickListener {
                viewModel.updateBookmark(Bookmark(url), Bookmark(editText.text.toString()))
                view.findNavController().navigate(R.id.nav_home)
            }
            view.findViewById<Button>(R.id.editBookmarkFragment_fetch).setOnClickListener{
                viewModel.updateBookmark(Bookmark(url), Bookmark(editText.text.toString()), BookmarksRepository.ShouldFetchTitle.Yes)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}