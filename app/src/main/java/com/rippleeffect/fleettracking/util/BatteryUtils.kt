package com.rippleeffect.fleettracking.util

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.os.BatteryManager
import android.os.PowerManager

object BatteryUtils {
    fun getBatteryPercentage(context: Context): Int {
        val bm: BatteryManager = context.getSystemService(Service.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    }

    fun isBatterySaverEnabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Service.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode


    }

    @SuppressLint("WakelockTimeout")
    fun acquireWakelock(context: Context) {
        val powerManager = context.getSystemService(Service.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com:rippleeffect:wakelock")
            .acquire()


    }


}