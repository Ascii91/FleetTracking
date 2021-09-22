package com.rippleeffect.fleettracking.repository

import com.google.gson.Gson
import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.model.TestModel
import com.rippleeffect.fleettracking.repository.db.AppDatabase
import com.rippleeffect.fleettracking.repository.network.FleetTrackerService
import com.rippleeffect.fleettracking.repository.sharedprefs.FleetTrackerPrefs
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import org.joda.time.DurationFieldType
import retrofit2.Response


class FleetTrackerRepository constructor(
    private val fleetTrackerService: FleetTrackerService,
    private val appDatabase: AppDatabase,
    private val fleetTrackerPrefs: FleetTrackerPrefs
) {

    suspend fun loadData(): Response<List<TestModel>> {
        return fleetTrackerService.getTodos()
    }

    suspend fun saveLocationRecord(locationRecord: LocationRecord): Long {

        fleetTrackerPrefs.lastSavedRecord = Gson().toJson(locationRecord)
        return appDatabase.locationRecordsDao().insert(locationRecord)

    }

    fun getLastSavedLocationRecord(): LocationRecord {
        if (fleetTrackerPrefs.lastSavedRecord == "") return LocationRecord()
        return Gson().fromJson(fleetTrackerPrefs.lastSavedRecord, LocationRecord::class.java)
    }


    fun getAllLocationRecords(): Flow<List<LocationRecord>> {
        return appDatabase.locationRecordsDao().getAllLocationRecords()
    }

    suspend fun getLocationRecordsByDay(currentDay: Long): List<LocationRecord> {

        val today = DateTime(currentDay).withMillisOfDay(0);
        val tomorrow = today.withFieldAdded(DurationFieldType.days(), 1)

        return appDatabase.locationRecordsDao()
            .getAllLocationRecordsByDay(today.millis, tomorrow.millis)
    }


    fun setPermissionsGranted(granted: Boolean) {
        fleetTrackerPrefs.arePermissionsGranted = granted
    }

    fun arePermissionGranted(): Boolean {
        return fleetTrackerPrefs.arePermissionsGranted
    }

    suspend fun getAllDatesInMillis(): List<Long> {
        return appDatabase.locationRecordsDao().getAllLocationRecordsSingle().map {
            it.timeInMillis
        }.distinctBy {
            DateTime(it).dayOfYear
        }.sortedBy {
            it
        }

    }

    fun isAlarmEnabled(): Boolean {
        return fleetTrackerPrefs.isAlarmEnabled
    }

    fun setAlarmEnabled(enabled: Boolean) {
        fleetTrackerPrefs.isAlarmEnabled = enabled
    }

    fun setFilteringEnabled(enabled: Boolean) {
        fleetTrackerPrefs.isFilteringEnabled = enabled
    }

    fun isFilteringEnabled(): Boolean {
        return fleetTrackerPrefs.isFilteringEnabled

    }


}