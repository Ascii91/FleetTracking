package com.rippleeffect.fleettracking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
class AlarmBroadcastReceiver() : BroadcastReceiver() {

    @Inject
    lateinit var fleetTrackerRepository: FleetTrackerRepository


    override fun onReceive(context: Context, intent: Intent?) {

        if (intent == null) return
        val currentLocationRecord = fleetTrackerRepository.getLastSavedLocationRecord()
        currentLocationRecord.timeInMillis = System.currentTimeMillis()
        currentLocationRecord.bateryPercentage = BatteryUtils.getBatteryPercentage(context)
        currentLocationRecord.origin = RecordOrigin.ALARM.ordinal
        currentLocationRecord.isPowerSaverActivated = BatteryUtils.isBatterySaverEnabled(context)


        GlobalScope.launch(Dispatchers.IO) {
            val id = fleetTrackerRepository.saveLocationRecord(currentLocationRecord)
            Timber.d("Inserted record: ID:$id")    ///
        }


    }
}