package com.rippleeffect.fleettracking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import com.rippleeffect.fleettracking.model.RecordOrigin
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import com.rippleeffect.fleettracking.util.BatteryUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ActivityTransitionBroadcastReceiver() : BroadcastReceiver() {

    @Inject
    lateinit var fleetTrackerRepository: FleetTrackerRepository


    override fun onReceive(context: Context, intent: Intent?) {

        if (intent == null) return
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent) ?: return
            for (event in result.transitionEvents) {

                val currentLocationRecord = fleetTrackerRepository.getLastSavedLocationRecord()
                currentLocationRecord.timeInMillis = System.currentTimeMillis()
                currentLocationRecord.bateryPercentage = BatteryUtils.getBatteryPercentage(context)
                currentLocationRecord.origin = RecordOrigin.ACTIVITY_RECOGNITION.ordinal
                currentLocationRecord.isPowerSaverActivated =
                    BatteryUtils.isBatterySaverEnabled(context)
                currentLocationRecord.detectedActivity = event.activityType
                currentLocationRecord.transitionType = event.transitionType

                GlobalScope.launch(Dispatchers.IO) {
                    val id = fleetTrackerRepository.saveLocationRecord(currentLocationRecord)
                    Timber.d("Inserted record: ID:$id")    ///
                }


            }
        }
    }
}