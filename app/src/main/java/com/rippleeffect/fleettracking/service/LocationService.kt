package com.rippleeffect.fleettracking.service

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.*
import com.rippleeffect.fleettracking.R
import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.model.RecordOrigin
import com.rippleeffect.fleettracking.mvvm.main.MainActivity
import com.rippleeffect.fleettracking.repository.FleetTrackerRepository
import com.rippleeffect.fleettracking.util.BatteryUtils.getBatteryPercentage
import com.rippleeffect.fleettracking.util.BatteryUtils.isBatterySaverEnabled
import com.rippleeffect.fleettracking.util.LocationUtils.generateActivityTransitions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import pl.tajchert.nammu.Nammu
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class LocationService : Service() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "fleettracker.location"
        const val CHANNEL_NAME = "Tracking Service"
        const val RESTART_SERVICE = "restart_service"
        const val ACTION_START = "action_start"
        const val ACTION_RECEIVE_MESSAGE = "action_receive_message"
        const val DETECTED_ACTIVITY = "detected_activity"
        const val TRANSITION_TYPE = "transition_type"
        private const val TRANSITIONS_RECEIVER_ACTION =
            "com.rippleeffect.fleettracking.ACTION_PROCESS_ACTIVITY_TRANSITIONS"

        const val ALARM_DELAY_IN_SECONDS = 15 * 60

        fun getLocationServiceIntent(context: Context, action: String): Intent {
            val serviceIntent = Intent(context, LocationService::class.java)
            serviceIntent.action = action
            return serviceIntent
        }

    }

    @Inject
    lateinit var fleetTrackerRepository: FleetTrackerRepository
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var activityRecognitionPendingIntent: PendingIntent
    private lateinit var receiver: ActivityTransitionBroadcastReceiver
    private var errorTitle = ""
    private var errorDescription = ""
    private var locationPermissionEnabled = true
    private var activityRecognitionPermissionEnabled = true

    private val currentLocationRecord = LocationRecord()


    private fun createPeriodicWork() {
        val workManager = WorkManager.getInstance(this)
        workManager.cancelAllWork()


        val work = PeriodicWorkRequestBuilder<PeriodicWorker>(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS
        ).build()


        val repeatingRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS
        ).build()

        workManager.enqueue(work)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        Nammu.init(this)

        checkPermissions(true)

        if (locationPermissionEnabled) {
            requestLocationUpdates()
        }
        if (activityRecognitionPermissionEnabled) {
            createActivityRecognitionRequests()
        }


        subscribeToLocationChanges()

    }

    private fun subscribeToLocationChanges() {

        GlobalScope.launch(Dispatchers.IO) {
            fleetTrackerRepository.getAllLocationRecords().collect {
                Timber.d("DB_COUNT:" + it.count())
                scheduleAlarm()
            }

        }


    }

    private fun checkPermissions(initialCheck: Boolean = false) {

        locationPermissionEnabled =
            Nammu.checkPermission(ACCESS_FINE_LOCATION)
        activityRecognitionPermissionEnabled =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Nammu.checkPermission(Manifest.permission.ACTIVITY_RECOGNITION) && Nammu.checkPermission(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }
        val powerSaverEnabled = isBatterySaverEnabled(this)


        val changeOccured =
            (locationPermissionEnabled && activityRecognitionPermissionEnabled && !powerSaverEnabled) != fleetTrackerRepository.arePermissionGranted()




        if (!locationPermissionEnabled) {
            errorTitle = getString(R.string.location_tracking_paused)
            errorDescription = getString(R.string.enable_location_permission)
            fleetTrackerRepository.setPermissionsGranted(false)
        } else if (!activityRecognitionPermissionEnabled) {
            errorTitle = getString(R.string.location_tracking_paused)
            errorDescription = getString(R.string.enable_activity_recognition)
            fleetTrackerRepository.setPermissionsGranted(false)
        } else if (powerSaverEnabled) {
            errorTitle = getString(R.string.batery_saver_is_on)
            errorDescription = getString(R.string.turn_off_batery_saver)
            fleetTrackerRepository.setPermissionsGranted(false)
        } else {
            fleetTrackerRepository.setPermissionsGranted(true)
            errorTitle = ""
            errorDescription = ""
        }

        if (initialCheck) {
            startNotifications()
        } else if (changeOccured) {

            startNotifications()

        }

    }

    private fun startNotifications() {
        Timber.d("START NOTIFICATIONS ***********")
        createNotificationChanel()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
            1,
            Notification()
        )

    }


    private fun createNotificationChanel() {

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val intent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_NONE
                )
            channel.setShowBadge(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(channel)
        }


        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setPriority(NotificationManager.IMPORTANCE_NONE)
            .setContentTitle(if (TextUtils.isEmpty(errorTitle)) "FleetTracking" else errorTitle)
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentText(if (TextUtils.isEmpty(errorDescription)) getString(R.string.fleet_tracking_running) else errorDescription)
            .setContentIntent(intent)
            .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Timber.d("ON_START COMMAND %s", intent?.action)
        when (intent?.action) {
            ACTION_START -> {
                createPeriodicWork()
            }
            ACTION_RECEIVE_MESSAGE -> {
                currentLocationRecord.detectedActivity = intent.getIntExtra(DETECTED_ACTIVITY, -1)
                currentLocationRecord.transitionType = intent.getIntExtra(TRANSITION_TYPE, -1)

                saveCurrentRecord(RecordOrigin.ACTIVITY_RECOGNITION)
            }


        }


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundJobs()
        val broadcastIntent = Intent()
        broadcastIntent.action = RESTART_SERVICE
        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
        this.sendBroadcast(broadcastIntent)
        job.cancel()
        cancelAlarm()
    }


    private fun stopBackgroundJobs() {
        WorkManager.getInstance(this).cancelAllWork()
        ActivityRecognition.getClient(this)
            .removeActivityTransitionUpdates(activityRecognitionPendingIntent)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 1000 * 5
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }
    }

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION])
    private fun requestLocationUpdates() {
        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        // received, store the location in Firebase

        client.requestLocationUpdates(createLocationRequest(), object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    updateLocationData(it.lastLocation)
                }

            }
        }, Looper.getMainLooper())


    }


    private fun updateLocationData(lastLocation: Location) {

        currentLocationRecord.accuracy = lastLocation.accuracy
        currentLocationRecord.altitude = lastLocation.altitude
        currentLocationRecord.isFromMockProvider = lastLocation.isFromMockProvider
        currentLocationRecord.speed = lastLocation.speed
        currentLocationRecord.latitude = lastLocation.latitude
        currentLocationRecord.longitude = lastLocation.longitude
        currentLocationRecord.provider = lastLocation.provider
        saveCurrentRecord(RecordOrigin.LOCATION)
    }


    private fun createActivityRecognitionRequests() {

        receiver = ActivityTransitionBroadcastReceiver()


        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiver,
            IntentFilter(TRANSITIONS_RECEIVER_ACTION)
        )

        val mIntentService = Intent(this, ActivityTransitionBroadcastReceiver::class.java)
        activityRecognitionPendingIntent = PendingIntent.getBroadcast(
            this, 1,
            mIntentService, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val request = ActivityTransitionRequest(generateActivityTransitions())

        ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, activityRecognitionPendingIntent)
            .addOnFailureListener {
                it.printStackTrace()
                Timber.d("Error creating activity recognition request")

            }.addOnSuccessListener {
                Timber.d("Success creating activity recognition request")
            }

    }

    private fun saveCurrentRecord(origin: RecordOrigin) {
        checkPermissions()

        currentLocationRecord.timeInMillis = System.currentTimeMillis()
        currentLocationRecord.bateryPercentage = getBatteryPercentage(this)
        currentLocationRecord.isActivityRecognitionPermissionEnabled =
            activityRecognitionPermissionEnabled
        currentLocationRecord.isLocationPermissionEnabled = locationPermissionEnabled
        currentLocationRecord.origin = origin.ordinal
        currentLocationRecord.isPowerSaverActivated = isBatterySaverEnabled(this)



        scope.launch {
            val id = fleetTrackerRepository.saveLocationRecord(currentLocationRecord)
            Timber.d("Inserted record: ID:$id")
        }
    }


    private fun scheduleAlarm() {
        if (!fleetTrackerRepository.isAlarmEnabled()) return
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.cancel(pendingIntent)

        val alarmTimeAtUTC = System.currentTimeMillis() + ALARM_DELAY_IN_SECONDS * 1000L

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTimeAtUTC,
            pendingIntent
        )

    }

    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }


}

