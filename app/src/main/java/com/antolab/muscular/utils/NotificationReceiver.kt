package com.antolab.muscular.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotificationReceiver", "Received notification broadcast")

        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Default Title"
        val content = intent?.getStringExtra(EXTRA_CONTENT) ?: "Default Content"

        // use the NotificationHelper to show the notification
        NotificationHelper(context!!).showNotification(title, content)

    }
}
