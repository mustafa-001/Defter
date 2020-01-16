package com.example.bookmarkmanager1.data

import  androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val bId: Long?,
    val url: String,
    val title: String = url, // this.fetchTitle()
    val favicon: String = "asd"
){
    override fun toString() = url
}

