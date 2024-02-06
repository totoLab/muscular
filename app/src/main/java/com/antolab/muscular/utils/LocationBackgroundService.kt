package com.antolab.muscular.utils

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.antolab.muscular.MyApplication
import com.antolab.muscular.MyApplication.Companion.appDao
import com.antolab.muscular.geocoding.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class LocationBackgroundService : Service() {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH)
    private val timeZoneId = ZoneId.of("Etc/UTC")
    private lateinit var lastNotificationSent: ZonedDateTime

    private val handler = Handler()
    private lateinit var notificationHelper: NotificationHelper
    private val defaultGymQuery = if (true) "via nicola giunta" else "via marconi rende"
    private val minDistance: Double = 3.0 * 1000.0f

    private val LOGGING_TAG = "geocoding"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOGGING_TAG, "service can't start for lack of location permission")
            stopSelf();
            return START_NOT_STICKY;
        }
        notificationHelper = NotificationHelper(this)
        handler.postDelayed(locationTask, 45*60*100) // Execute every 4.5 minutes
        Log.d(LOGGING_TAG, "service $this started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val locationTask = object : Runnable {
        override fun run() {
            getCurrentLocation { updatedLocation ->
                searchPoint(defaultGymQuery) { geoPoint ->
                    if (geoPoint != null) {
                        val distance = distanceBetween(updatedLocation, geoPoint)
                        Log.d(LOGGING_TAG, "Distance from gym ($geoPoint): $distance")
                        if (distance < minDistance) {
                            val zdtNow = ZonedDateTime.now()
                            if (!::lastNotificationSent.isInitialized || hasTimeElapsed(lastNotificationSent, zdtNow, 15)) {
                                lastNotificationSent = zdtNow
                                sendNotification(zdtNow)
                            }
                        } else {
                            Log.d(LOGGING_TAG, "Troppo distante dalla palestra ${distance}m")
                        }
                    } else {
                        Toast.makeText(this@LocationBackgroundService, "Impossibile trovare la palestra", Toast.LENGTH_LONG).show()
                    }
                }
                handler.postDelayed(this, 60000)
            }
        }
    }

    private fun hasTimeElapsed(start: ZonedDateTime, end: ZonedDateTime, minutes: Long): Boolean {
        val duration: Duration = Duration.between(start, end)
        return duration.toMinutes() > minutes
    }

    private fun sendNotification(now: ZonedDateTime) {
        Log.d(LOGGING_TAG, "last notification was sent $lastNotificationSent")
        val lang = loadLocate()
        val database = MyApplication.appDatabase
        appDao = database.appDao()

        MainScope().launch {
            if (appDao.getExercisesCount() == 0) {
                Log.d(LOGGING_TAG, "no programmes to use for the notification")
                return@launch
            } else {
                val programmes = appDao.getAllProgrammes()
                val total = programmes.size
                val day: Int = now.dayOfWeek.value - 1
                val index = (day + total) % total
                val card = programmes[index]

                val card_name = when (lang) {
                    "en" -> card.name_en
                    "es" -> card.name_es
                    "de" -> card.name_de
                    "fr" -> card.name_fr
                    "it" -> card.name_it
                    else -> card.name_it
                }
                notificationHelper.sendCustomNotification("Pronto per allenarti", "Oggi scheda: $card_name")
            }
        }
    }

    private fun loadLocate(): String {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("My_Lang", "it") ?: "it"
    }
    private fun searchPoint(query: String, callback: (GeoPoint?) -> Unit) {
        val clientGeocoding = NominatimClient.create()

        val geocodeCall = clientGeocoding.geocode(query)
        geocodeCall.enqueue(object : Callback<List<GeocodingResult>> {
            override fun onResponse(call: Call<List<GeocodingResult>>, response: Response<List<GeocodingResult>>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (!res.isNullOrEmpty()) {
                        val firstResult = res[0]
                        val geoPoint = GeoPoint(firstResult.lat, firstResult.lon)
                        callback(geoPoint)
                    } else {
                        Log.d(LOGGING_TAG, "response was empty ${response.raw()}")
                        callback(null)
                    }
                } else {
                    Log.d(LOGGING_TAG, "response was not successful ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<GeocodingResult>>, t: Throwable) {
                Log.d(LOGGING_TAG, "failed to send request")
                t.printStackTrace()
                callback(null)
            }
        })
    }

    private fun distanceBetween(g1: GeoPoint, g2: GeoPoint) : Double {
        return g1.distanceToAsDouble(g2)
    }

    @SuppressLint("MissingPermission") // todo: ask for permissions
    private fun getCurrentLocation(callback: (GeoPoint) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val myLocation = if (location != null) {
                GeoPoint(location.latitude, location.longitude)
            } else {
                GeoPoint(0, 0) // Default location if last location is null
            }
            Log.d(LOGGING_TAG, "last recorded location is $myLocation")
            callback.invoke(myLocation)
        }
    }
    override fun onDestroy() {
        // Remove any pending callbacks when the service is destroyed
        handler.removeCallbacks(locationTask)
        super.onDestroy()
    }
}
