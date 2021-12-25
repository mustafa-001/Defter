package com.ktdefter.defter.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.File

class EditBookmarkFragment : Fragment() {

    private val viewModel: BookmarksViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().fab.hide()
        val url = requireArguments().getString("url")!!
        return inflater.inflate(R.layout.edit_bookmark_fragment, container, false).also { view ->
            val editText = view.findViewById<EditText>(R.id.editBookmarkFragment_url)
            editText.setText(url)
            val titleText = view.findViewById<EditText>(R.id.editBookmarkFragment_title)
            val faviconImageView = view.findViewById<ImageView>(R.id.editBookmarkFragment_favicon)

            viewModel.getBookmark(url).observe(viewLifecycleOwner) { bookmark ->
                if (url != editText.text.toString() || bookmark == null){
                    return@observe
                }
                titleText.setText(bookmark.title)
                File(requireContext().filesDir, bookmark.hostname).let {
                    faviconImageView.setImageURI(Uri.fromFile(it))
                }
            }

            view.findViewById<Button>(R.id.editBookmarkFragment_fetch).setOnClickListener {
//                viewModel.addBookmark(
//                    Bookmark(editText.text.toString()),
//                    BookmarksRepository.ShouldFetchTitle.Yes
//                )
                viewModel.bookmarksRepository.fetchMetadata(Bookmark(editText.text.toString())).observe(viewLifecycleOwner) { bookmark ->
                    if (bookmark == null){
                        return@observe
                    }
                    titleText.setText(bookmark.title)
                    File(requireContext().filesDir, bookmark.hostname).let {
                        faviconImageView.setImageURI(Uri.fromFile(it))
                    }
                }
            }

            view.findViewById<Button>(R.id.editBookmarkFragment_submit).setOnClickListener {
                viewModel.replaceBookmark(
                    Bookmark(url),
                    Bookmark(editText.text.toString(), titleText.text.toString()),
                    BookmarksRepository.ShouldFetchTitle.IfNeeded
                )
                view.findNavController().navigate(R.id.nav_home)
            }

        }
    }

    override fun onDestroy() {
        requireActivity().fab.show()
        super.onDestroy()
    }

}