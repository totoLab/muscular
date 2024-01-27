package com.antolab.muscular

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.antolab.muscular.MyApplication.Companion.appDao
import com.antolab.muscular.utils.PrePopulation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.lifecycle.lifecycleScope


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = MyApplication.appDatabase
        appDao = database.appDao()

        val navButtonStats: Button = findViewById(R.id.nav_button_stats)
        navButtonStats.setOnClickListener {
            startNewActivity(StatsActivity::class.java)
        }

        val navButtonWorkouts: Button = findViewById(R.id.nav_button_workouts)
        navButtonWorkouts.setOnClickListener {
            startNewActivity(WorkoutsActivity::class.java)
        }

        val navButtonExercises: Button = findViewById(R.id.nav_button_exercises)
        navButtonExercises.setOnClickListener {
            startNewActivity(ExercisesActivity::class.java)
        }

        val langButton: Button = findViewById(R.id.bnLang)
        langButton.setOnClickListener {
            showChangeLang()
        }

        loadLocate()

        // Example notification
        val notificationHelper = NotificationHelper(this)

        notificationHelper.sendCustomNotification("CM", "U cunnu e mammata")
        // Example periodic notification
        notificationHelper.sendPeriodicNotification("CZ", "Cunnu e ziata", 60 * 1000, true)



        //esempio di notifica periodica, con true invia anche una notifica all'avvio, altrimenti no
        notificationHelper.sendPeriodicNotification(
            "Fresh Time!",
            "Mantieniti idratato durante la tua giornata!",
            60 * 60 * 1000,
            true) // Ogni ora in millisecondi
    }

    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
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

        // Get the current language
        val currentLang = loadLocate()
        val langIndex: Int = listItems.indexOf(countriesMap[currentLang])

        // Create an AlertDialog
        val mBuilder = AlertDialog.Builder(this@MainActivity)
        mBuilder.setTitle(R.string.ad_title)

        mBuilder.setSingleChoiceItems(listItems, langIndex) { dialog, which ->
            when (which) {
                0 -> setLocate("en") // English
                1 -> setLocate("es") // Español
                2 -> setLocate("fr") // Français
                3 -> setLocate("it") // Italiano
                4 -> setLocate("de") // Deutsch
            }
            recreate()

            // Call updateUIElements within a coroutine
            GlobalScope.launch {
                updateUIElements()
            }

            dialog.dismiss()
        }

        val mDialog = mBuilder.create()
        mDialog.show()
    }


    private fun setLocate(lang: String) {
        currentLanguage = lang
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", lang)
        editor.apply()

        // Update UI elements based on the selected language
        updateUIElements()
    }

    private fun updateUIElements() {
        currentLanguage = loadLocate()
        // Use the lifecycleScope for coroutine in an AppCompatActivity
        lifecycleScope.launch {
            // Retrieve exercises or other elements from the database based on the current language
            val exercises = appDao.getAllExercises()

            // Update UI elements with the new language-specific data
            for (exercise in exercises) {
                val name: String = when (currentLanguage) {
                    "en" -> exercise.name_en
                    "es" -> exercise.name_es
                    "fr" -> exercise.name_fr
                    "it" -> exercise.name_it
                    "de" -> exercise.name_de
                    else -> exercise.name_en // Default to English if language is not recognized
                }

                val description: String = when (currentLanguage) {
                    "en" -> exercise.description_en
                    "es" -> exercise.description_es
                    "fr" -> exercise.description_fr
                    "it" -> exercise.description_it
                    "de" -> exercise.description_de
                    else -> exercise.description_it // Default to English if language is not recognized
                }

                // Update UI elements using the name and description
                // You can use these values to update TextViews, etc.
            }
        }
    }





    private lateinit var currentLanguage: String

    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        currentLanguage = sharedPreferences.getString("My_Lang", "") ?: ""
        return currentLanguage
    }

}