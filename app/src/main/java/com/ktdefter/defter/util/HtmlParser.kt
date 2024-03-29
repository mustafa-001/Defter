package com.ktdefter.defter.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.ktdefter.defter.data.Bookmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import timber.log.Timber

fun getTitleAndFaviconAsync(context: Context, url: Uri): Deferred<Bookmark> = CoroutineScope(Dispatchers.Default).async {
    val newUrl = if (url.scheme == "http") {
        url.buildUpon().scheme("https").build()
    } else url

    val doc = try {
        Jsoup.connect(newUrl.toString())
            .timeout(30000)
            .followRedirects(true)
            .get()
    } catch (e: Exception) {
        Timber.d(e.toString())
        return@async Bookmark(url.toString(), null, null)
    }
    Timber.d("requesting: $url")
    val imageUrl: String? = doc.select("link[href~=.*\\.(ico|png)]").first()?.absUrl("href")

    val hostName = Bookmark(newUrl.toString()).hostname

    if (imageUrl == null) {
        Timber.d("Failed to parse site favicon.")
    } else {
        Timber.d("Downloading image at: $imageUrl")
        saveImage(context, hostName, imageUrl)
    }

    return@async Bookmark(newUrl.toString(), doc.title(), hostName)
}

fun saveImage(context: Context, url: String, image_url: String): String? {
    val bitmap: Bitmap = try {
        downloadImage(image_url)
    } catch (e: Exception) {
        Timber.d("Cannot retrieve favicon from $image_url $e")
        return null
    }

    context.openFileOutput(url, Context.MODE_PRIVATE).use { fos ->
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
    if (!response.isSuccessful) {
        throw okio.IOException("Error when downloading image: $response")
    } else {
        return response.body?.byteStream()
            .let { stream ->
                BitmapFactory.decodeStream(stream)
            }
    }
}
