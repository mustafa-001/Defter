package com.ktdefter.defter.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ktdefter.defter.R
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.Tag
import com.ktdefter.defter.viewmodels.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bookmark_list.*
import timber.log.Timber

@AndroidEntryPoint
class SelectTagDialogFragment : DialogFragment() {
    private var listener: OnFragmentInteractionListener? = null
    private val bookmarksViewModel: BookmarksViewModel by viewModels()
    lateinit var selectedBookmark: Bookmark
    lateinit var tags: List<Tag>
    val changes: MutableMap<String, Boolean> = mutableMapOf()
    private lateinit var allTags: List<Tag>

    private inner class TagsListAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return allTags.size
        }

        override fun getItem(position: Int): Any {
            return allTags[position]
        }

        override fun getItemId(position: Int): Long {
            return allTags[position].tagName.hashCode().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view =
                convertView ?: layoutInflater.inflate(R.layout.tag, bookmarks_list_fragment, false)
            return view.apply {
                val checkBox = findViewById<CheckBox>(R.id.tag_checkbox)
                checkBox.apply {
                    isChecked = tags.any { it.tagName == allTags[position].tagName }
                    setOnClickListener {
                        changes[allTags[position].tagName] = isChecked
                    }
                }
                findViewById<TextView>(R.id.tag_text).apply {
                    text = allTags[position].tagName
                }
                findViewById<LinearLayout>(R.id.tag_selector).apply {
                    setOnClickListener {
                        checkBox.isChecked = !checkBox.isChecked
                        changes[allTags[position].tagName] = checkBox.isChecked
                    }
                }

            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return activity?.let { activity ->
            allTags.map { tags.contains(it) }
            Timber.d("all tags are: " + allTags.map { it.tagName + " " })
            val view =
                activity.layoutInflater.inflate(R.layout.fragment_select_tag_dialog, null)
            val negativeButton = view.findViewById<Button>(R.id.new_tag_cancel)
            negativeButton.setOnClickListener {
                dialog!!.cancel()

            }
            val positiveBotton = view.findViewById<Button>(R.id.new_tag_add)
            positiveBotton.setOnClickListener {
                onPositiveClick(
                    changes,
                    view.findViewById<EditText>(R.id.new_tag_text).text.toString()
                )
                dialog!!.cancel()
            }
            val tagsList = view.findViewById<ListView>(R.id.tags_list_view)
            tagsList.adapter = TagsListAdapter()

            AlertDialog.Builder(activity)
                .setView(view)
                .create()

        } ?: throw IllegalStateException("Main Activity cannot be null")
    }

    private fun onPositiveClick(changes: MutableMap<String, Boolean>, newTag: String) {
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
        if (newTag != "") {
            bookmarksViewModel.addBookmarkTagPair(
                selectedBookmark.url,
                newTag
            )
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
