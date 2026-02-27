package com.example.taximeter

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var textViewDistance: TextView
    private lateinit var textViewTime: TextView
    private lateinit var textViewFare: TextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var cbBaggage: CheckBox
    private lateinit var cbNight: CheckBox
    private lateinit var cbAnimal: CheckBox
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button

    private var distanceMeters = 0.0
    private var elapsedTime = 0L

    companion object {
        private const val LOCATION_PERMISSION_CODE = 1000
        private const val NOTIFICATION_PERMISSION_CODE = 2000
    }

    // Tarifs
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
        spinnerCategory = findViewById(R.id.spinnerCategory)
        cbBaggage = findViewById(R.id.cbBaggage)
        cbNight = findViewById(R.id.cbNight)
        cbAnimal = findViewById(R.id.cbAnimal)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)

        val categories = arrayOf("A", "B", "C", "D")
        spinnerCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        btnStart.setOnClickListener { checkPermissionAndStart() }
        btnStop.setOnClickListener { stopMeter() }
        btnReset.setOnClickListener { resetDisplay() }
    }

    private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        distanceMeters = intent?.getDoubleExtra(LocationService.EXTRA_DISTANCE, 0.0) ?: 0.0
        elapsedTime = intent?.getLongExtra(LocationService.EXTRA_TIME, 0L) ?: 0L
        updateDisplay(distanceMeters, elapsedTime)
    }
}

    override fun onResume() {
    super.onResume()
    val filter = IntentFilter(LocationService.ACTION_UPDATE)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }
}

override fun onPause() {
    super.onPause()
    unregisterReceiver(receiver)
}

    private fun updateDisplay(distanceMeters: Double, elapsedTime: Long) {
    val totalSeconds = elapsedTime / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    textViewTime.text = String.format("Temps: %02d:%02d:%02d", hours, minutes, seconds)

    val distanceKm = distanceMeters / 1000
    textViewDistance.text = String.format("Distance: %.2f km", distanceKm)

    val category = spinnerCategory.selectedItem.toString()
    var fare = baseFare[category]!! + perKmRate[category]!! * distanceKm
    if (cbBaggage.isChecked) fare += baggageFee
    if (cbNight.isChecked) fare += nightFee
    if (cbAnimal.isChecked) fare += animalFee

    textViewFare.text = String.format("Tarif: %.2f €", fare)
}

    private fun resetDisplay() {
        textViewDistance.text = "Distance: 0.0 km"
        textViewTime.text = "Temps: 00:00:00"
        textViewFare.text = "Tarif: 0.0 €"
    }

    private fun startMeter() {
        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_START
        startService(intent)
    }

    private fun stopMeter() {
        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_STOP
        startService(intent)
    }

    private fun checkPermissionAndStart() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
                return
            }
        }

        startMeter()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            checkPermissionAndStart()
        }

        if (requestCode == NOTIFICATION_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startMeter()
        }
    }
}
