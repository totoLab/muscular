package com.antolab.muscular

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.antolab.muscular.MyApplication.Companion.appDao
import com.antolab.muscular.utils.LocationBackgroundService
import com.antolab.muscular.utils.NotificationHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var notificationHelper: NotificationHelper

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    private val GEOLOCATION_PERMISSION_REQUEST_CODE = 2

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

        // Check and request notification permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Display a rationale to the user
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                // Show an explanation to the user
                val builder = AlertDialog.Builder(this)
                builder.setMessage("We need notification permissions to keep you updated on important events.")
                builder.setPositiveButton(
                    "OK"
                ) { dialog, which -> // Request the permission again
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
                builder.show()
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            notificationHelper = NotificationHelper(this)

            //esempio di notifica periodica, con true invia anche una notifica all'avvio, altrimenti no
            notificationHelper.sendPeriodicNotification(
                getString(R.string.FreshTime), getString(R.string.StayHydrated),
                60 * 60 * 1000, true)
        }

        // Check and request geolocation permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Display a rationale to the user
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user
                val builder = AlertDialog.Builder(this)
                builder.setMessage("We need geolocation permissions to provide location-based services.")
                builder.setPositiveButton(
                    "OK"
                ) { dialog, which -> // Request the permission again
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                        GEOLOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                builder.show()
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    GEOLOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            startService(LocationBackgroundService::class.java)
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    notificationHelper = NotificationHelper(this)

                    //esempio di notifica periodica, con true invia anche una notifica all'avvio, altrimenti no
                    notificationHelper.sendPeriodicNotification(
                        getString(R.string.FreshTime), getString(R.string.StayHydrated),
                        60 * 60 * 1000, true)
                }
            }
            GEOLOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(LocationBackgroundService::class.java)
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun startService(serviceClass: Class<*>) {
        Log.d("services", "starting service ${serviceClass.canonicalName}")
        val serviceIntent = Intent(this, serviceClass)
        startService(serviceIntent)
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
    }

    private lateinit var currentLanguage: String

    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        currentLanguage = sharedPreferences.getString("My_Lang", "") ?: ""
        return currentLanguage
    }

}