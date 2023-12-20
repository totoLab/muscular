package com.antolab.muscular
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



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

        // esempio di notifica
        val notificationHelper = NotificationHelper(this)

        //esempio di notifica periodica, con true invia anche una notifica all'avvio, altrimenti no
        notificationHelper.sendPeriodicNotification(
            "Fresh Time!",
            "Mantieniti idratato durante la tua giornata!",
            60 * 60 * 1000,
            false) // Ogni ora in millisecondi
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

        // ottieni la lingua corrente
        val currentLang = loadLocate()
        val langIndex: Int = listItems.indexOf(countriesMap[currentLang])

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

    private fun loadLocate(): String? {
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        return sharedPreferences.getString("My_Lang", "")
    }


}