package com.ktdefter.defter.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.ktdefter.defter.data.Bookmark
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup


fun getTitleAndFavicon(context: Context, url: String): Bookmark {
    val doc = Jsoup.connect(url)
        .timeout(30000)
        .get()
    Log.d("Defter", "requesting: $url")
    val imageUrl: String? = doc.select("link[href~=.*\\.(ico|png)]").first()?.absUrl("href")

    val hostName = Bookmark(url).getHostname()

    if (imageUrl == null){
        Log.d("Defter", "Failed to parse site favicon.")
    } else {
        Log.d("Defter", "Downloading image at: $imageUrl")
        saveImage(context, hostName, imageUrl)
    }

    return Bookmark(url, doc.title(), hostName)
}

fun saveImage(context: Context, url: String, image_url: String) :String?{
    val bitmap:Bitmap = try {
        downloadImage(image_url)
    } catch (e: okio.IOException){
        Log.d("Defter", "Cannot retrieve favicon from $image_url")
        return null
    }

    context.openFileOutput(url, Context.MODE_PRIVATE ).use {fos ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos)
        fos.close()
    }
    return url
}

//Return a sealed class from here.
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
