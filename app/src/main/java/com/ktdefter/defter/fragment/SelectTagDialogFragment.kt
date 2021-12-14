package com.ktdefter.defter.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_tag_dialog.view.*
import timber.log.Timber

@AndroidEntryPoint
class SelectTagDialogFragment : DialogFragment() {
    private var listener: OnFragmentInteractionListener? = null
    private  val bookmarksViewModel: BookmarksViewModel by viewModels()
    lateinit var selectedBookmark: Bookmark
    lateinit var tags: List<Tag>
    private lateinit var allTags: List<Tag>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return activity?.let {activity ->

            val selectedTags = allTags.map { tags.contains(it) }
            Timber.d("all tags are: " + allTags.map{it.tagName+" "})
            val changes: MutableMap<String, Boolean> = mutableMapOf()
            val view = activity.layoutInflater.inflate(R.layout.fragment_select_tag_dialog, null)
            view.apply {
                new_tag_text.setAdapter(
                    ArrayAdapter(this.context,
                        android.R.layout.simple_dropdown_item_1line,
                        allTags.map { t -> t.tagName })
                )
                new_tag_text.threshold = 1
            }

            AlertDialog.Builder(activity)
                .setView(view)

                .setMultiChoiceItems(allTags.map { it.tagName }.toTypedArray(),
                    allTags.map { tags.contains(it) }.toBooleanArray()
                ) { _, which, isChecked ->
                    changes[allTags.map { it.tagName }[which]] = isChecked
                }

                .setTitle("Select new tag")

                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

                .setPositiveButton("Add"
                ) { _, _ ->
                    changes.mapKeys {
                        if (it.value) {
                            bookmarksViewModel.addBookmarkTagPair(selectedBookmark.url, it.key)
                        } else {
                            bookmarksViewModel.deleteBookmarkTagPair(
                                selectedBookmark.url,
                                it.key
                            )
                        }
                    }
                    onPositiveClick(view.new_tag_text.text.toString())
                }
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
        allTags = bookmarksViewModel.getTagsSync()
        tags = bookmarksViewModel.getTagsOfBookmarkSync(selectedBookmark.url)
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
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
