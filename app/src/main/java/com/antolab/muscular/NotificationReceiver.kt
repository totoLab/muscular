package com.antolab.muscular

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            // Chiamata a showNotification per creare il canale di notifica
            showNotification(context)

            val notificationIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Aggiungi questa parte per mostrare la notifica
            val notification = NotificationCompat.Builder(context, "channel_id")
                .setContentTitle("Fresh time!")
                .setContentText("Ricordati di idratarti durante il tuo allenamento.")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification)
        }
    }

    // Sposta la definizione di showNotification qui
    private fun showNotification(context: Context) {
        // Creare un canale di notifica solo se il dispositivo è su Android Oreo (API 26) o versioni successive
        val channelId = "channel_id"
        val channelName = "Channel Name"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)

        // Registrare il canale con il NotificationManager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun setNotification(context: Context, notificationTime: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Imposta l'orario desiderato per la notifica
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime.timeInMillis, pendingIntent)
    }
}
