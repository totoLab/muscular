package com.antolab.muscular

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav_button_stats : Button = findViewById<Button>(R.id.nav_button_stats)
        nav_button_stats.setOnClickListener {
            val intent_stats = Intent(this, StatsActivity::class.java)
            startActivity(intent_stats)
        }

        val nav_button_workouts : Button = findViewById<Button>(R.id.nav_button_workouts)
        nav_button_workouts.setOnClickListener {
            val intent_workouts = Intent(this, WorkoutActivity::class.java)
            startActivity(intent_workouts)
        }

        val nav_button_exercises : Button = findViewById<Button>(R.id.nav_button_exercises)
        nav_button_exercises.setOnClickListener {
            val intent_exercises = Intent(this, ExercisesActivity::class.java)
            startActivity(intent_exercises)
        }


        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, 1) // Invia il broadcast ogni minuto

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            60 * 1000, // Ogni tre secondi in millisecondi
            pendingIntent
        )
    }
}