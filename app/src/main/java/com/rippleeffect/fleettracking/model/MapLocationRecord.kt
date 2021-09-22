package com.rippleeffect.fleettracking.model

import org.joda.time.DateTime

class MapLocationRecord(
    var latitude: Double,
    var longitude: Double,
    var accuracy: Float,
    var startTime: Long,
    var endTime: Long,
) {


    fun getFormatedDuration(): String {
        return DateTime(startTime).toString("HH:mm") +
                if (endTime == 0L) "" else " - " + DateTime(endTime).toString("HH:mm")


    }

    fun isSingleTimePoint(): Boolean {
        return (startTime == endTime || endTime == 0L || (endTime - startTime) / 1000 < 300)
    }
}