package com.rippleeffect.fleettracking.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import javax.inject.Inject

class MyWorkerFactory @Inject constructor(private val fleetTrackerRepository: FleetTrackerRepository) :
    WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        // This only handles a single Worker, please don’t do this!!
        // See below for a better way using DelegatingWorkerFactory
        return PeriodicWorker(appContext, workerParameters, fleetTrackerRepository)

    }
}