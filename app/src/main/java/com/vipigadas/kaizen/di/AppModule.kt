package com.vipigadas.kaizen.di

import android.content.Context
import com.vipigadas.kaizen.features.sport.SportRepository
import com.vipigadas.kaizen.features.sport.SportRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides app-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the application context.
     * This is needed for accessing resources in non-composable classes.
     */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    /**
     * Provides a singleton instance of SportsRepository.
     * Using an interface allows for easier testing with mock implementations.
     */
    @Provides
    @Singleton
    fun provideSportRepository(impl: SportRepositoryImpl): SportRepository = impl
}