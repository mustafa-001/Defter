package com.ktdefter.defter.data
import  androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Tag(
    @PrimaryKey
    val tagName: String
)

{
    override fun toString(): String {
        return tagName
    }
}
