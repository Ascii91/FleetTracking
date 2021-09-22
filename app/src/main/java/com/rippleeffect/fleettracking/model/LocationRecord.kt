package com.rippleeffect.fleettracking.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.location.DetectedActivity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class LocationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var latitude: Double,
    var longitude: Double,
    var timeInMillis: Long,
    var detectedActivity: Int,
    var transitionType: Int,
    var accuracy: Float,
    var altitude: Double,
    var speed: Float,
    var bateryPercentage: Int,
    var isFromMockProvider: Boolean,
    var isLocationPermissionEnabled: Boolean,
    var isActivityRecognitionPermissionEnabled: Boolean,
    //0-Timed 1-Location 2-ActivityRecognition
    var origin: Int,
    var isPowerSaverActivated: Boolean,
    var provider:String

    ) : Parcelable {
    fun getTextFromActivity(): String {
        var result = "Unknown"
        result = when (transitionType) {
            0 -> "E"
            1 -> "X"
            else -> {
                return result
            }
        }
        result += when (detectedActivity) {
            DetectedActivity.IN_VEHICLE -> " " + "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> " " + "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> " " + "ON_FOOT"
            DetectedActivity.STILL -> " " + "STILL"
            DetectedActivity.UNKNOWN -> " " + "UNKNOWN"
            DetectedActivity.TILTING -> " " + "TILTING"
            DetectedActivity.RUNNING -> " " + "RUNNING"
            DetectedActivity.WALKING -> " " + "WALKING"
            else -> " " + "UNKNOWN"
        }
        return result

    }

    fun getOriginText(): String {
        return when (origin) {
            0 -> "T"
            1 -> "L"
            2 -> "A"
            3 -> "X"

            else->"-"
        }

    }

    constructor() : this(
        0,
        0.0,
        0.0,
        0,
        -1,
        -1,
        0.0f,
        0.0,
        0.0f,
        0,
        false,
        false,
        false,
        0,
        false,
        ""
    )

}
