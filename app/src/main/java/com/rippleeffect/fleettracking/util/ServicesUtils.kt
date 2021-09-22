package com.rippleeffect.fleettracking.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.rippleeffect.fleettracking.R
import timber.log.Timber


object ServicesUtils {

    const val PLAY_SERVICES_RESOLUTION_REQUEST = 1000

    fun isMyServiceRunning(serviceClass: Class<*>, mActivity: Activity): Boolean {
        val manager: ActivityManager =
            mActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun checkPlayServices(activity: Activity): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(activity)
        return if (result != ConnectionResult.SUCCESS) {
            //Any random request code
            //Any random request code

            //Google Play Services app is not available or version is not up to date. Error the
            // error condition here
            //Google Play Services app is not available or version is not up to date. Error the
            // error condition here
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.showErrorDialogFragment(
                    activity, result,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                ) {
                    activity.finishAffinity()
                }
            } else {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.play_services_required),
                    Toast.LENGTH_LONG
                ).show()
                activity.finishAffinity()
            }
            false
        } else {
            true
        }
        //Google Play Services is available. Return true.
    }




}