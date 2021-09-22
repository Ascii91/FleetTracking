package com.rippleeffect.fleettracking.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rippleeffect.fleettracking.model.RecordOrigin
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import com.rippleeffect.fleettracking.util.BatteryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class PeriodicWorker(
    val context: Context,
    workerParams: WorkerParameters,
    private val fleetTrackerRepository: FleetTrackerRepository
) :
    CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val currentLocationRecord = fleetTrackerRepository.getLastSavedLocationRecord()
        currentLocationRecord.timeInMillis = System.currentTimeMillis()
        currentLocationRecord.bateryPercentage = BatteryUtils.getBatteryPercentage(context)
        currentLocationRecord.origin = RecordOrigin.TIMED.ordinal
        currentLocationRecord.isPowerSaverActivated = BatteryUtils.isBatterySaverEnabled(context)

        val id = fleetTrackerRepository.saveLocationRecord(currentLocationRecord)
        Timber.d("Inserted record: ID:$id")


        return@withContext try {
            // Do something
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }


}

