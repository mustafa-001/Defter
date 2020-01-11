package com.example.bookmarkmanager1.data

import  androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey val url: String,
    val title: String = url, // this.fetchTitle()
    val favicon: String = "asd",
    //TODO Find a way to store tags.
    val tags: String = "implement tags" //emptySet<String>()
){
    override fun toString() = url
}

