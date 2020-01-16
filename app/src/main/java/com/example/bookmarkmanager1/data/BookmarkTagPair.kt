package com.example.bookmarkmanager1.data

import  androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkTagPair(
    @PrimaryKey(autoGenerate = true) val pId: Int,
    val bookmarkId: Int,
    val tagId: Int
)
