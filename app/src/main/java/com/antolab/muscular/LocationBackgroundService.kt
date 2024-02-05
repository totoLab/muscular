package com.antolab.muscular

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast

import org.osmdroid.util.GeoPoint

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.location.LocationServices

import com.antolab.muscular.geocoding.*


class LocationBackgroundService : Service() {

    private val handler = Handler()
    private lateinit var notificationHelper: NotificationHelper
    private val defaultGymQuery = if (true) "via nicola giunta" else "via marconi rende"
    private val minDistance: Double = 3.0 * 1000.0f

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        notificationHelper = NotificationHelper(this)
        // Schedule the periodic execution of your task
        handler.postDelayed(locationTask, 60*1000) // Execute every 1 minute

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Define your background task here
    private val locationTask = object : Runnable {
        override fun run() {
            setMapView { updatedLocation ->
                searchPoint(defaultGymQuery) { geoPoint ->
                    if (geoPoint != null) {
                        val distance = distanceBetween(updatedLocation, geoPoint)
                        Log.d("geocoding", "Distance from gym ($geoPoint): $distance")
                        if (distance < minDistance) {
                            // Trigger your notification here
                            notificationHelper.sendCustomNotification("Pronto per allenarti", "Oggi scheda: Gambe") // TODO: differentiate notification based on day of week
                        } else {
                            Log.d("geocoding", "Troppo distante dalla palestra ${distance}m")
                        }
                    } else {
                        Toast.makeText(this@LocationBackgroundService, "Impossibile trovare la palestra", Toast.LENGTH_LONG).show()
                    }
                }

                // Reschedule the task for periodic execution
                handler.postDelayed(this, 60000) // Execute every 1 minute
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

    @SuppressLint("MissingPermission") // todo: ask for permissions
    private fun setMapView(callback: (GeoPoint) -> Unit) {
        val TAG = "geocoding"
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val myLocation = if (location != null) {
                GeoPoint(location.latitude, location.longitude)
            } else {
                GeoPoint(0, 0) // Default location if last location is null
            }
            Log.d(TAG, "last recorded location is $myLocation")
            callback.invoke(myLocation)
        }
    }
    override fun onDestroy() {
        // Remove any pending callbacks when the service is destroyed
        handler.removeCallbacks(locationTask)
        super.onDestroy()
    }
}
