package com.ktdefter.defter

import android.util.Log
import com.ktdefter.defter.data.Bookmark
import com.ktdefter.defter.data.BookmarksInHTML
import com.ktdefter.defter.fragment.HTMLImporter
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.InputStream

class HTMLImporterTest {

    val HTMLStream: InputStream = BookmarksInHTML.byteInputStream()

    @Before
    fun setUp(){

    }
    @Test
    fun DeserializeFromHTMLToBookmark(){


        val bookmarks =   HTMLImporter(HTMLStream).import()
        Log.d("defter_test", bookmarks[1].url)
        assertTrue(bookmarks.map{it.url}.contains("http://www.righto.com/"))
    }
}