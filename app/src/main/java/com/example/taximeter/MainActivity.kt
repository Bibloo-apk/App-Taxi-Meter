package com.example.taximeter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button

    companion object {
        private const val LOCATION_PERMISSION_CODE = 1000
        private const val NOTIFICATION_PERMISSION_CODE = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)

        btnStart.setOnClickListener { checkPermissionAndStart() }
        btnStop.setOnClickListener { stopMeter() }
    }

    private fun checkPermissionAndStart() {

    // Vérifie GPS
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

    // 🔥 Vérifie Notification (Android 13+)
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

    private fun startMeter() {
        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_START
        startService(intent)
        Toast.makeText(this, "Compteur démarré 🚕", Toast.LENGTH_SHORT).show()
    }

    private fun stopMeter() {
        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_STOP
        startService(intent)
        Toast.makeText(this, "Compteur arrêté", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    when (requestCode) {

        LOCATION_PERMISSION_CODE -> {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                checkPermissionAndStart()
            } else {
                Toast.makeText(
                    this,
                    "Permission GPS nécessaire",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        NOTIFICATION_PERMISSION_CODE -> {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startMeter()
            } else {
                Toast.makeText(
                    this,
                    "Permission notification nécessaire",
                    Toast.LENGTH_LONG
                ).show()
                }
            }
        }
    }
}
