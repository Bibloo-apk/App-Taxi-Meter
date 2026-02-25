package com.example.taximeter

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private val baseFare = 4.48
    private val perKm = 1.30
    private val perHour = 42.15
    private val minimumFare = 8.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val distanceInput = findViewById<EditText>(R.id.distanceInput)
        val timeInput = findViewById<EditText>(R.id.timeInput)
        val resultText = findViewById<TextView>(R.id.resultText)
        val button = findViewById<Button>(R.id.calculateButton)

        button.setOnClickListener {
            val km = distanceInput.text.toString().toDoubleOrNull() ?: 0.0
            val minutes = timeInput.text.toString().toDoubleOrNull() ?: 0.0

            val distanceCost = km * perKm
            val timeCost = (minutes / 60.0) * perHour

            var total = baseFare + distanceCost + timeCost

            if (total < minimumFare) {
                total = minimumFare
            }

            total = round(total * 100) / 100
            resultText.text = "Tarif: %.2f €".format(total)
        }
    }
}
