package com.ktdefter.defter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

@Database(entities = [Bookmark::class, Tag::class, BookmarkTagPair::class], version = 2,
    exportSchema = true)
@TypeConverters(Converters::class)
abstract class BookmarksDatabase : RoomDatabase() {
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

        // TODO Don't allow queries from main thread, use coroutines
        private fun buildDatabase(context: Context): BookmarksDatabase {
            return Room.databaseBuilder(context, BookmarksDatabase::class.java, "bookmarks.db")
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                })
                .allowMainThreadQueries()
                .build()
        }
    }
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Bookmark` ADD COLUMN `last_modification` LONG NOT NULL DEFAULT ${Date().time}")
    }
}
