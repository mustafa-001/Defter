package com.ktdefter.defter.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Entity
@Serializable
data class Bookmark(
    @PrimaryKey
    val url: String,
    val title: String? = null,
    var favicon: String? = null,
) {
    override fun toString() = url
    fun getHostname(): String{
        return url
            .removePrefix("http://")
            .removePrefix("https://")
            .replaceAfter("/", "")
            .removePrefix("www.")
            .dropLastWhile { it -> it == '/'.toChar() }
    }

    override fun equals(other: Any?): Boolean {
        return (other as Bookmark).url == this.url
    }

    @Ignore var tags: List<Tag> = emptyList() }

