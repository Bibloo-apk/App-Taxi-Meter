package com.example.taximeter

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var textViewFare: TextView
    private lateinit var textViewTime: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private var running = false
    private var secondsElapsed = 0
    private var totalDistance = 0.0 // en km
    private var fare = 0.0

    private val handler = Handler(Looper.getMainLooper())
    private val formatter = DecimalFormat("0.00")

    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null

    // Tarifs par catégorie
    private val tariffs = mapOf(
        "A" to Tariff(base = 3.40, perKm = 1.05, perMinute = 0.35),
        "B" to Tariff(base = 4.00, perKm = 1.20, perMinute = 0.40),
        "C" to Tariff(base = 5.00, perKm = 1.50, perMinute = 0.50),
        "D" to Tariff(base = 6.00, perKm = 2.00, perMinute = 0.60)
    )

    private var selectedCategory = "A"

    // Suppléments
    private val supplementBaggage = 1.0
    private val supplementNight = 2.0
    private val supplementAnimal = 1.5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewFare = findViewById(R.id.textViewFare)
        textViewTime = findViewById(R.id.textViewTime)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        btnStart.setOnClickListener {
            if (!running) {
                running = true
                secondsElapsed = 0
                totalDistance = 0.0
                fare = 0.0
                lastLocation = null
                startTimer()
                startLocationUpdates()
            }
        }

        btnStop.setOnClickListener {
            running = false
            stopLocationUpdates()
        }

        updateDisplay()
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (running) {
                    secondsElapsed++

                    calculateFare()
                    updateDisplay()

                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun calculateFare() {
        val tariff = tariffs[selectedCategory]!!
        val timeMinutes = secondsElapsed / 60.0
        fare = tariff.base + totalDistance * tariff.perKm + timeMinutes * tariff.perMinute

        // ici tu peux ajouter les suppléments selon la logique
        // exemple: fare += supplementBaggage, supplementNight etc.
    }

    private fun updateDisplay() {
        val minutes = secondsElapsed / 60
        val seconds = secondsElapsed % 60
        textViewTime.text = "Temps: $minutes:${if (seconds < 10) "0$seconds" else "$seconds"}"
        textViewFare.text = "Tarif: €${formatter.format(fare)}"
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, this)
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        lastLocation?.let {
            totalDistance += it.distanceTo(location) / 1000.0 // distance en km
        }
        lastLocation = location
    }

    data class Tariff(val base: Double, val perKm: Double, val perMinute: Double)
}
