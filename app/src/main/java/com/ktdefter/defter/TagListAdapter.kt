//package com.ktdefter.defter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.ktdefter.defter.data.Tag
//
//class TagListAdapter() : RecyclerView.Adapter<TagListAdapter.TagViewHolder>(){
//    lateinit var tags: List<Tag>
//
//    class TagViewHolder(v: View):RecyclerView.ViewHolder(v){
//        val tagname: TextView? = null
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
//        return TagViewHolder(
//            LayoutInflater
//                .from(context)
//                .inflate(R.findView, false)
//        )
//
//    }
//
//    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
//        holder.tag_text = "Tag $position"
//    }
//
//    override fun getItemCount(): Int {
//
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}