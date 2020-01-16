package com.example.bookmarkmanager1.data
import  androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val tId: Int?,
    val tagName: String
)

{
    override fun toString(): String {
        return tagName
    }
}
