package com.ktdefter.defter

import android.util.Log
import com.ktdefter.defter.fragment.HTMLImporter
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.InputStream

class HTMLImporterTest {

    private val hTMLStream: InputStream = BookmarksInHTML.byteInputStream()

    @Before
    fun setUp(){

    }
    @Test
    fun deserializeFromHTMLToBookmark(){


        val bookmarks =   HTMLImporter(hTMLStream).import()
        Log.d("defter_test", bookmarks[1].url)
        assertTrue(bookmarks.map{it.url}.contains("http://www.righto.com/"))
    }
}