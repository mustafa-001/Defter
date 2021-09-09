package com.ktdefter.defter.data
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.ktdefter.defter.util.getTitleAndFavicon
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(ActivityComponent::class)
object DatabaseModule {

    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        BookmarksDatabase::class.java,
        "bookmarks.db"
    ).build() // The reason we can construct a database for the repo

    @Provides
    fun provideBookmarkDao(db: BookmarksDatabase) = db.bookmarkDao() // The reason we can implement a Dao for the database

    @Provides
    fun provideTagDao(db: BookmarksDatabase) = db.tagDao() // The reason we can implement a Dao for the database

    @Provides
    fun provideBookmarkTagPairDao(db: BookmarksDatabase) = db.bookmarkTagPairDao() // The reason we can implement a Dao for the database
}

// TODO Fetch url title and favicon if they don't exist in database.
class BookmarksRepository @Inject constructor(
    private val bookmarksDao: BookmarkDao,
    private val tagDao: TagDao,
    private val bookmarkTagPairDao: BookmarkTagPairDao,
    @ActivityContext private  val context: Context
) {
    fun getBookmarks():LiveData<List<Bookmark>> = bookmarksDao.getBookmarks()
    fun getBookmarksSync():List<Bookmark> = bookmarksDao.getBookmarksSync()
    fun getBookmark(url: String)= bookmarksDao.getBookmark(url)

    fun insertBookmark(url: String) {
        bookmarksDao.insertBookmark(Bookmark(url))
        Log.d("Defter", "Inserting bookmark: $url")
        GlobalScope.launch {
           val bookmark = async {  getTitleAndFavicon(context, url) }.await()
            bookmarksDao.insertBookmark(bookmark)
        }
    }

    fun deleteBookmark(url: String) = bookmarksDao.deleteBookmark(url)

    fun insertTag(tag: String) = tagDao.insertTag(Tag(tag))

    fun getTags() = tagDao.getTags()

    fun getTagsSync() = tagDao.getTagsSync()

    fun deleteTag(tagName: String) = tagDao.deleteTag(tagName)

    fun addBookmarkTagPair(url: String, tag: String) {
        bookmarkTagPairDao.addBookmarkTagPair(url, tag)
    }
    fun deleteBookmarkTagPair(url: String, tag: String) = bookmarkTagPairDao.deletePair(url, tag)

    fun getTagsOfBookmark(url: String) = bookmarkTagPairDao.getTagsWithBookmark(url)

    fun getTagsOfBookmarkSync(url: String) = bookmarkTagPairDao.getTagsWithBookmarkList(url)

    fun getBookmarksOfTag(tag: String) = bookmarkTagPairDao.getBookmarksWithTag(tag)

    fun getBookmarksOfTagSync(tag: String) = bookmarkTagPairDao.getBookmarksWithTagSync(tag)

    companion object {

        @Volatile private var instance: BookmarksRepository? = null

        fun getInstance(bookmarksDao: BookmarkDao, tagDao: TagDao, bookmarkTagPairDao: BookmarkTagPairDao, context: Context): BookmarksRepository {
            return instance ?: synchronized(this) {
                instance ?: BookmarksRepository(bookmarksDao, tagDao, bookmarkTagPairDao, context).also { instance = it }
            }
        }
    }
}
