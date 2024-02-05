package com.antolab.muscular

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.antolab.muscular.MyApplication.Companion.appDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.antolab.muscular.geocoding.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.*
import org.osmdroid.views.*
import org.osmdroid.views.overlay.mylocation.*
import retrofit2.*

class MainActivity : AppCompatActivity() {
    private lateinit var notificationHelper: NotificationHelper
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
        notificationHelper = NotificationHelper(this)

        //esempio di notifica periodica, con true invia anche una notifica all'avvio, altrimenti no
        notificationHelper.sendPeriodicNotification(
            getString(R.string.FreshTime),
            getString(R.string.StayHydrated),
            60 * 60 * 1000,
            true) // Ogni ora in millisecondi
    }

    override fun onStart() {
        super.onStart()

        // todo: move this thing in a place to be executed periodically in background
        val map = findViewById<MapView>(R.id.map)
        map.visibility = View.GONE
        val myLocation: GeoPoint = setMapView(map)
        val defaultGymQuery = "via marconi rende"
        val minDistance: Double = 1.5 * 1000.0f
        searchPoint(defaultGymQuery) { geoPoint ->
            if (geoPoint != null) {
                val distance = distanceBetween(myLocation, geoPoint)
                Log.d("geocoding", "$distance")
                if (distance < minDistance) {
                    notificationHelper.sendCustomNotification("Pronto per allenarti", "Oggi scheda: Gambe") // TODO: differentiate notification based on day of week
                } else {
                    Log.d("geocoding", "Troppo distante dalla palestra ${distance}m")
                }
            } else {
                Toast.makeText(this@MainActivity, "Impossibile trovare la palestra", Toast.LENGTH_LONG).show()
            }
        }

    }
    private fun searchPoint(query: String, callback: (GeoPoint?) -> Unit) {
        val clientGeocoding = NominatimClient.create()

        val geocodeCall = clientGeocoding.geocode(query)
        val TAG = "geocoding"
        geocodeCall.enqueue(object : Callback<List<GeocodingResult>> {
            override fun onResponse(call: Call<List<GeocodingResult>>, response: Response<List<GeocodingResult>>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (!res.isNullOrEmpty()) {
                        val firstResult = res[0]
                        val geoPoint = GeoPoint(firstResult.lat, firstResult.lon)
                        callback(geoPoint)
                    } else {
                        Log.d(TAG, "response was empty ${response.raw()}")
                        callback(null)
                    }
                } else {
                    Log.d(TAG, "response was not successful ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<GeocodingResult>>, t: Throwable) {
                Log.d(TAG, "failed to send request")
                t.printStackTrace()
                callback(null)
            }
        })
    }

    private fun distanceBetween(g1: GeoPoint, g2: GeoPoint) : Double {
        return g1.distanceToAsDouble(g2)
    }

    @SuppressLint("MissingPermission") // todo: enable permissions
    private fun setMapView(view: MapView) : GeoPoint {
        view.controller.setZoom(17.0)
        view.setMultiTouchControls(true)
        view.setTileSource(TileSourceFactory.MAPNIK)

        /* Definizione un overlay che tramite un GPS provider Leqge la posizione corrente del dispositivo */
        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), view) // todo: remove view from deps of the function
        myLocationOverlay.enableMyLocation()
        view.overlays.add(myLocationOverlay)
        myLocationOverlay.isDrawAccuracyEnabled = true

        /* Alliavvio dell'app, al run dell'UI-Thread, viene impostato il centro della mappa sulla posizione corrente */
        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                view.controller.setCenter(myLocationOverlay.myLocation)
                view.controller.animateTo(myLocationOverlay.myLocation)
            }
        }
        val defaultPoint = if (true) GeoPoint(39.3540611680632, 16.23071131350694) else GeoPoint(0, 0)
        return myLocationOverlay.myLocation ?: defaultPoint
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