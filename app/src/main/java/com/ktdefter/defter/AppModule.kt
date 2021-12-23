package com.ktdefter.defter

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ktdefter.defter.data.BookmarksDatabase
import com.ktdefter.defter.data.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBookmarksDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        BookmarksDatabase::class.java,
        "bookmarks.db"
    // TODO: don't use main thread, use coroutines instead. see. https://stackoverflow.com/questions/44167111/android-room-simple-select-query-cannot-access-database-on-the-main-thread
    ).allowMainThreadQueries()
        .addMigrations(MIGRATION_1_2)
        .build() // The reason we can construct a database for the repo

    @Provides
    @Singleton
    fun provideFireStore() = Firebase.firestore


    @Singleton
    @Provides
    fun provideBookmarkDao(db: BookmarksDatabase) = db.bookmarkDao() // The reason we can implement a Dao for the database

    @Singleton
    @Provides
    fun provideTagDao(db: BookmarksDatabase) = db.tagDao() // The reason we can implement a Dao for the database

    @Singleton
    @Provides
    fun provideBookmarkTagPairDao(db: BookmarksDatabase) = db.bookmarkTagPairDao() // The reason we can implement a Dao for the database

//    @Provides
//    fun provideAuthViewModel(authViewModel: BookmarksViewModel): ViewModel {
//    }
}
