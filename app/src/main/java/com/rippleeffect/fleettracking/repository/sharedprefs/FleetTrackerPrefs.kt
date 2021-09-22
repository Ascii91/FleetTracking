package com.rippleeffect.fleettracking.repository.sharedprefs

import android.content.Context
import com.chibatching.kotpref.KotprefModel

class FleetTrackerPrefs(context: Context) : KotprefModel(context) {
    var isFilteringEnabled: Boolean by booleanPref(true)
    var isAlarmEnabled: Boolean by booleanPref(true)
    var arePermissionsGranted: Boolean by booleanPref(true)
    var lastSavedRecord: String by stringPref("")

}