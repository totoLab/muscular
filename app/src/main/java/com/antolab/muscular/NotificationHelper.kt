package com.antolab.muscular

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "my_channel_id"
        private const val CHANNEL_NAME = "My Channel"
        private const val CHANNEL_DESCRIPTION = "Description of my channel"
    }



    // Metodo per inviare una notifica periodica con titolo, contenuto e intervallo di tempo
    fun sendPeriodicNotification(title: String, content: String, intervalMillis: Long, instant : Boolean) {
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        notificationIntent.putExtra(NotificationReceiver.EXTRA_TITLE, title)
        notificationIntent.putExtra(NotificationReceiver.EXTRA_CONTENT, content)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(), // Utilizza un ID univoco basato sul tempo attuale
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        // Calcola il prossimo avviso dopo l'intervallo specificato
        val nextOccurrence = calendar.timeInMillis + intervalMillis

        // Imposta l'allarme per la notifica periodica
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextOccurrence,
            intervalMillis,
            pendingIntent
        )

        // Invia anche una notifica immediata
        if (instant) {
            showNotification(title, content)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }




    fun sendCustomNotification(title: String, content: String) {
        val notificationId = System.currentTimeMillis().toInt()
        showNotification(title, content, notificationId)
    }




    fun showNotification(
        title: String,
        content: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            notify(notificationId, builder.build())
        }
    }

    fun sendUpdatableNotification(title: String, content: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            // Use the same notificationId to update the existing notification
            notify(notificationId, builder.build())
        }
    }

    fun cancelNotification(notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }

}
