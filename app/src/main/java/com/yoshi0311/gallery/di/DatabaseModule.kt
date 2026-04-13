package com.yoshi0311.gallery.di

import android.content.Context
import androidx.room.Room
import com.yoshi0311.gallery.data.local.db.FavoriteDao
import com.yoshi0311.gallery.data.local.db.GalleryDatabase
import com.yoshi0311.gallery.data.local.db.TrashDao
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
    fun provideDatabase(@ApplicationContext context: Context): GalleryDatabase =
        Room.databaseBuilder(context, GalleryDatabase::class.java, "gallery.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteDao(db: GalleryDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideTrashDao(db: GalleryDatabase): TrashDao = db.trashDao()
}
