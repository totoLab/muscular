package com.antolab.muscular

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav_button_stats : Button = findViewById<Button>(R.id.nav_button_stats)
        nav_button_stats.setOnClickListener {
            val intent_stats : Intent = Intent(this, StatsActivity::class.java)
            startActivity(intent_stats)
        }

        val nav_button_workouts : Button = findViewById<Button>(R.id.nav_button_workouts)
        nav_button_workouts.setOnClickListener {
            val intent_workouts : Intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent_workouts)
        }

        val nav_button_exercises : Button = findViewById<Button>(R.id.nav_button_exercises)
        nav_button_exercises.setOnClickListener {
            val intent_exercises : Intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent_exercises)
        }
    }
}