package com.rippleeffect.fleettracking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.rippleeffect.fleettracking.mvvm.control.ControlFragment

class RestartBackgroundService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("Broadcast Listened", "Service tried to stop")
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show()


        val isFromUser = ControlFragment.isServiceStoppedFromUser

        if (isFromUser) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(Intent(context, LocationService::class.java))
        } else {
            context?.startService(Intent(context, LocationService::class.java))
        }
    }
}