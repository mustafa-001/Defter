package com.ktdefter.defter.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Entity
@Serializable
data class Bookmark(
    @PrimaryKey
    val url: String,
    val title: String? = null,
    var favicon: String? = null,
    @Contextual
    val lastModification: Date = Date(),
    @Contextual
    val hostname: String = getHostname(url),
    @field:JvmField
    val isDeleted: Boolean = false
) {

    override fun toString() = url
    override fun equals(other: Any?): Boolean {
        return (other as Bookmark).url == this.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }


    @Ignore
    @Contextual
    var isSelected: Boolean = false

    @Ignore
    var tags: List<Tag> = emptyList()
}


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return value.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

}

fun getHostname(url: String): String {
    return url
        .removePrefix("http://")
        .removePrefix("https://")
        .replaceAfter("/", "")
        .removePrefix("www.")
        .dropLastWhile { it -> it == '/'.toChar() }
}
