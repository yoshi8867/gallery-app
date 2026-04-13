package com.yoshi0311.gallery.di

import com.yoshi0311.gallery.data.repository.AlbumRepository
import com.yoshi0311.gallery.data.repository.AlbumRepositoryImpl
import com.yoshi0311.gallery.data.repository.FavoriteRepository
import com.yoshi0311.gallery.data.repository.FavoriteRepositoryImpl
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.MediaRepositoryImpl
import com.yoshi0311.gallery.data.repository.TrashRepository
import com.yoshi0311.gallery.data.repository.TrashRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindTrashRepository(impl: TrashRepositoryImpl): TrashRepository
}
