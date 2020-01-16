package com.example.bookmarkmanager1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [Bookmark::class,Tag::class,BookmarkTagPair::class], version = 2, exportSchema = false)
abstract class BookmarksDatabase : RoomDatabase(){
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tagDao(): TagDao
    abstract fun bookmarkTagPairDao(): BookmarkTagPairDao

    companion object {

        @Volatile private var INSTANCE: BookmarksDatabase? = null

        fun getInstance(context: Context): BookmarksDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        //TODO Don't allow queries from main thread, use coroutines
        private fun buildDatabase(context: Context): BookmarksDatabase {
            return Room.databaseBuilder(context, BookmarksDatabase::class.java, "bookmarks.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        //getInstance(context).bookmarksDao().insertBookmark(Bookmark("buzlarcozulmeden.com"))
//                        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
//                        WorkManager.getInstance(context).enqueue(request)
                    }
                })
                .allowMainThreadQueries()
                .build()
        }

    }
}

