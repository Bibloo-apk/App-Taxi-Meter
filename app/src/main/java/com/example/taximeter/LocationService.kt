package com.example.taximeter

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.*
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var distanceMeters = 0.0
    private var lastLocation: Location? = null
    private var startTime = 0L

    companion object {
        const val CHANNEL_ID = "TaxiMeterChannel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val MIN_DISTANCE_METERS = 2f
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val EXTRA_DISTANCE = "EXTRA_DISTANCE"
        const val EXTRA_TIME = "EXTRA_TIME"
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        )
            .setMinUpdateIntervalMillis(500)
            .build()

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }

        return START_STICKY
    }

    private fun startTracking() {

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Taximètre actif 🚕")
            .setContentText("Le trajet est en cours...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
        startTime = System.currentTimeMillis()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private val locationCallback = object : LocationCallback() {
    override fun onLocationResult(result: LocationResult) {

        val location = result.lastLocation ?: return
        if (location.accuracy > 20) return

        if (lastLocation != null) {
            val distance = lastLocation!!.distanceTo(location)
            if (distance > MIN_DISTANCE_METERS) {
                distanceMeters += distance
            }
        }

        lastLocation = location

        val elapsedTime = System.currentTimeMillis() - startTime

        val intent = Intent(ACTION_UPDATE)
        intent.putExtra(EXTRA_DISTANCE, distanceMeters)
        intent.putExtra(EXTRA_TIME, elapsedTime)
        sendBroadcast(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "TaxiMeter Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
