package com.yoshi0311.gallery.di

import com.yoshi0311.gallery.data.repository.AlbumRepository
import com.yoshi0311.gallery.data.repository.AlbumRepositoryImpl
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.MediaRepositoryImpl
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
}
