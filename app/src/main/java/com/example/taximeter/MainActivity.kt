package com.example.taximeter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var textViewFare: TextView
    private lateinit var textViewTime: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private var running = false
    private var secondsElapsed = 0
    private var distanceKm = 0.0
    private var fare = 0.0

    private val handler = Handler(Looper.getMainLooper())
    private val formatter = DecimalFormat("0.00")

    // Tarifs en euros
    private val baseFare = 3.0       // prise en charge
    private val perKm = 1.5          // par km
    private val perMinute = 0.3      // par minute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewFare = findViewById(R.id.textViewFare)
        textViewTime = findViewById(R.id.textViewTime)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        btnStart.setOnClickListener {
            if (!running) {
                running = true
                startTimer()
            }
        }

        btnStop.setOnClickListener {
            running = false
        }

        // initial display
        textViewFare.text = "Tarif: €${formatter.format(fare)}"
        textViewTime.text = "Temps: 0:00"
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (running) {
                    secondsElapsed++
                    distanceKm += 0.05 // simule 50m par seconde, tu peux ajuster

                    // calcul du tarif
                    fare = baseFare + (distanceKm * perKm) + ((secondsElapsed / 60.0) * perMinute)

                    // mise à jour interface
                    val minutes = secondsElapsed / 60
                    val seconds = secondsElapsed % 60
                    textViewTime.text = "Temps: $minutes:${if (seconds < 10) "0$seconds" else "$seconds"}"
                    textViewFare.text = "Tarif: €${formatter.format(fare)}"

                    handler.postDelayed(this, 1000)
                }
            }
        })
    }
}
