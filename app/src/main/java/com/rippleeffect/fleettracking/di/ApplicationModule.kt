package com.rippleeffect.fleettracking.di

import android.content.Context
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import com.rippleeffect.fleettracking.repository.db.AppDatabase
import com.rippleeffect.fleettracking.repository.network.FleetTrackerService
import com.rippleeffect.fleettracking.repository.sharedprefs.FleetTrackerPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return AppDatabase.buildDatabase(appContext)
    }


    @Provides
    @Singleton
    fun provideAppPrefs(@ApplicationContext appContext: Context): FleetTrackerPrefs {
        return FleetTrackerPrefs(appContext)
    }

    @Provides
    @Singleton
    fun provideFleetTrackerRepository(
        fleetTrackerService: FleetTrackerService,
        appDatabase: AppDatabase,
        fleetTrackerPrefs: FleetTrackerPrefs
    ): FleetTrackerRepository {
        return FleetTrackerRepository(fleetTrackerService, appDatabase, fleetTrackerPrefs)
    }


}