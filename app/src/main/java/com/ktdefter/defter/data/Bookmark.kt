package com.ktdefter.defter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey
    val url: String,
    val title: String = "Title isn't implemented",
    val favicon: String = "Not implemented yet"
) {
    override fun toString() = url
    fun getHostname(): String = TODO()
    var tags = "empty"
}
