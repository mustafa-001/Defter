package com.ktdefter.defter.util

import android.graphics.Bitmap
import android.util.Log
import com.ktdefter.defter.data.Bookmark
import org.jsoup.Jsoup


suspend fun getTitleAndFavicon(url: String): Bookmark {
    val doc = Jsoup.connect(url)
        .timeout(30000)
        .get()
    Log.d("Defter", "requesting: $url")
    return Bookmark(url, doc.title())
}

fun saveImage(png: Bitmap) {
    TODO()
}
