package com.ktdefter.defter.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.ktdefter.defter.data.Bookmark
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URI


fun getTitleAndFavicon(context: Context, url: String): Bookmark {
    val doc = Jsoup.connect(url)
        .timeout(30000)
        .get()
    Log.d("Defter", "requesting: $url")
    val imageUrl = "https://cdn.sstatic.net/Sites/stackoverflow/img/favicon.ico?v=4f32ecc8f43d"//doc.getElementById()
    val hostName = Bookmark(url).getHostname()

    saveImage(context, hostName, imageUrl)

    return Bookmark(url, doc.title(), hostName)
}

fun saveImage(context: Context, url: String, image_url: String) :String{
    val bitmap:Bitmap = downloadImage(image_url)

    context.openFileOutput(url, Context.MODE_PRIVATE ).use {fos ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos)
        fos.close()
    }
    return url
}

fun downloadImage(url: String): Bitmap {
    val client = OkHttpClient()
    val request: Request = Request.Builder()
        .url(url)
        .build()

    val response = client.newCall(request).execute()
    return if (!response.isSuccessful) {
        throw okio.IOException("Error when downloading image: $response")
    } else {
        response.body?.byteStream().let { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }
}
