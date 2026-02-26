package com.example.taximeter

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var textViewDistance: TextView
    private lateinit var textViewTime: TextView
    private lateinit var textViewFare: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button
    private lateinit var spinnerCategory: Spinner
    private lateinit var cbBaggage: CheckBox
    private lateinit var cbNight: CheckBox
    private lateinit var cbAnimal: CheckBox

    private var running = false
    private var startTime = 0L
    private var elapsedTime = 0L

    private var distanceMeters = 0.0
    private var lastLocation: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Tarifs officiels (exemple)
    private val baseFare = mapOf("A" to 3.0, "B" to 4.0, "C" to 5.0, "D" to 6.0)
    private val perKmRate = mapOf("A" to 1.5, "B" to 2.0, "C" to 2.5, "D" to 3.0)
    private val baggageFee = 1.0
    private val nightFee = 2.0
    private val animalFee = 1.5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewDistance = findViewById(R.id.textViewDistance)
        textViewTime = findViewById(R.id.textViewTime)
        textViewFare = findViewById(R.id.textViewFare)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        cbBaggage = findViewById(R.id.cbBaggage)
        cbNight = findViewById(R.id.cbNight)
        cbAnimal = findViewById(R.id.cbAnimal)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val categories = arrayOf("A", "B", "C", "D")
        spinnerCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        btnStart.setOnClickListener {
            startMeter()
        }

        btnStop.setOnClickListener {
            stopMeter()
        }

        btnReset.setOnClickListener {
            resetMeter()
        }
    }

    private fun startMeter() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        running = true
        startTime = SystemClock.elapsedRealtime() - elapsedTime
        lastLocation = null
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create().apply {
                interval = 2000
                fastestInterval = 1000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            },
            locationCallback,
            null
        )

        runTimer()
    }

    private fun stopMeter() {
        running = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun resetMeter() {
        running = false
        elapsedTime = 0L
        distanceMeters = 0.0
        lastLocation = null
        textViewDistance.text = "Distance: 0.0 km"
        textViewTime.text = "Temps: 00:00:00"
        textViewFare.text = "Tarif: 0.0 €"
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (!running) return
            val location = result.lastLocation ?: return
            if (lastLocation != null) {
                distanceMeters += lastLocation!!.distanceTo(location)
            }
            lastLocation = location
            updateDisplay()
        }
    }

    private fun runTimer() {
        Thread {
            while (running) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime
                runOnUiThread { updateDisplay() }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun updateDisplay() {
        // Temps
        val totalSeconds = elapsedTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        textViewTime.text = String.format("Temps: %02d:%02d:%02d", hours, minutes, seconds)

        // Distance
        val distanceKm = distanceMeters / 1000
        textViewDistance.text = String.format("Distance: %.2f km", distanceKm)

        // Tarif
        val category = spinnerCategory.selectedItem.toString()
        var fare = baseFare[category]!! + perKmRate[category]!! * distanceKm
        if (cbBaggage.isChecked) fare += baggageFee
        if (cbNight.isChecked) fare += nightFee
        if (cbAnimal.isChecked) fare += animalFee

        textViewFare.text = String.format("Tarif: %.2f €", fare)
    }
}
