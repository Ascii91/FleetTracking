package com.rippleeffect.fleettracking.repository.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rippleeffect.fleettracking.model.LocationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationRecordsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationRecord): Long


    @Query("SELECT * FROM locationrecord order by id desc")
    fun getAllLocationRecords(): Flow<List<LocationRecord>>

    @Query("SELECT * FROM locationrecord where timeInMillis>=:startOfDay and timeInMillis<=:endOfDay order by id desc")
    suspend fun getAllLocationRecordsByDay(startOfDay: Long, endOfDay: Long): List<LocationRecord>



    @Query("SELECT * FROM locationrecord order by id desc")
    suspend fun getAllLocationRecordsSingle(): List<LocationRecord>
}