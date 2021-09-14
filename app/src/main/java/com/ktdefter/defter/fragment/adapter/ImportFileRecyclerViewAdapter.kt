package com.ktdefter.defter.fragment.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ktdefter.defter.R

import com.ktdefter.defter.placeholder.PlaceholderContent.PlaceholderItem
import kotlinx.android.synthetic.main.fragment_select_import_file.view.*
import java.io.File

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class ImportFileRecyclerViewAdapter(
    private val values: Array<File>
) : RecyclerView.Adapter<ImportFileRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.fragment_select_import_file, parent, false)
            )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = position.toString()
        holder.contentView.text = item.path
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: View) :
        RecyclerView.ViewHolder(binding) {
        val idView: TextView = binding.item_number
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}