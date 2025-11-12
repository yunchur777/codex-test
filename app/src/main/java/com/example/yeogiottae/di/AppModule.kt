package com.example.yeogiottae.di

import android.content.Context
import com.example.yeogiottae.data.AccommodationRepository
import com.example.yeogiottae.data.FakeAccommodationRepository
import com.example.yeogiottae.location.FusedLocationProvider
import com.example.yeogiottae.location.LocationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAccommodationRepository(): AccommodationRepository = FakeAccommodationRepository()

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context
    ): LocationProvider = FusedLocationProvider(context)
}
