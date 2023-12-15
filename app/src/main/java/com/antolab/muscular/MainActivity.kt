package com.antolab.muscular

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.util.Calendar
import androidx.appcompat.app.AlertDialog           //importazione della Classe: AlertDialog
import java.util.*
import android.app.Activity
import android.content.Context                      //importazione della Classe: Context
import android.content.res.Configuration            //importazione della Classe: Configuration


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load saved language preference or set default to "it"
        val currentLanguage = loadLocate()
        setLocate(currentLanguage)


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
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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

        // Set up language change dialog
        val langButton: Button = findViewById(R.id.bnLang)
        langButton.setOnClickListener {
            showChangeLang()
        }

        // Load saved language preference
        loadLocate()
    }

    private fun showChangeLang() {
        val countriesMap = mapOf(
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "it" to "Italiano",
            "de" to "Deutsch"
            // Add more countries as needed
        )

        val listItems = countriesMap.values.toTypedArray()

        // ottieni la lingua corrente
        val currentLang = loadLocate()
        val langIndex : Int = listItems.indexOf(countriesMap.get(currentLang))

        // crea un AlertDialog
        val mBuilder = AlertDialog.Builder(this@MainActivity)
        mBuilder.setTitle(R.string.ad_title)

        mBuilder.setSingleChoiceItems(listItems, langIndex) { dialog, which ->
            when (which) {
                0 -> {
                    setLocate("en") // English
                    recreate()
                }
                1 -> {
                    setLocate("es") // Español
                    recreate()
                }
                2 -> {
                    setLocate("fr") // Français
                    recreate()
                }
                3 -> {
                    setLocate("it") // Italiano
                    recreate()
                }
                4 -> {
                    setLocate("de") // Tedesco
                    recreate()
                }
            }
            dialog.dismiss()
        }

        val mDialog = mBuilder.create()
        mDialog.show()
    }

    private fun setLocate(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", lang)
        editor.apply()
    }

    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        return sharedPreferences.getString("My_Lang", "it") ?: "it"
    }
}

