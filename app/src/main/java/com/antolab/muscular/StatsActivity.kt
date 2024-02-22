package com.antolab.muscular

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val button_back : Button = findViewById<Button>(R.id.button_stats_back)
        button_back.setOnClickListener {
            finish()
        }
    }
}